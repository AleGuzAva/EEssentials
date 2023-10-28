package EEssentials.commands.teleportation;

import EEssentials.EEssentials;
import EEssentials.storage.PlayerStorage;
import EEssentials.storage.StorageManager;
import EEssentials.util.Location;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Defines the /home related commands to manage player homes.
 */
public class HomeCommands {

    public static final String SET_HOME_PERMISSION_NODE = "eessentials.sethome";
    public static final String DELETE_HOME_PERMISSION_NODE = "eessentials.delhome";
    public static final String HOME_PERMISSION_NODE = "eessentials.home.self";
    public static final String HOME_OTHER_PERMISSION_NODE = "eessentials.home.other";


    /**
     * Registers home related commands (/sethome, /delhome, /home, /homes).
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Set a new home or overwrite an existing one for the player.
        dispatcher.register(literal("sethome")
                .requires(src -> Permissions.check(src, SET_HOME_PERMISSION_NODE, 2))
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String homeName = StringArgumentType.getString(ctx, "name");
                            if (player == null) return 0;

                            PlayerStorage playerStorage = EEssentials.storage.getPlayerStorage(player);

                            // If the new home name isn't already a home, we should check if the player has enough sethomes left
                            if (!playerStorage.homes.containsKey(homeName)) {
                                if (playerStorage.homes.keySet().size() >= EEssentials.perms.getMaxHomes(player)) {
                                    player.sendMessage(Text.literal("You've reached your maximum amount of homes (" + EEssentials.perms.getMaxHomes(player) + "), delete one to set another."));
                                    return 0;
                                }
                            }

                            playerStorage.homes.put(
                                    homeName,
                                    Location.fromPlayer(player)
                            );
                            playerStorage.save();

                            player.sendMessage(Text.literal("Home " + homeName + " has been set to the current location."), false);
                            return 1;
                        })
                )
        );

        // Delete a home for the player.
        dispatcher.register(literal("delhome")
                .then(argument("name", StringArgumentType.word())
                        .requires(src -> Permissions.check(src, DELETE_HOME_PERMISSION_NODE, 2))
                        .suggests(HomeCommands::suggestHomes)
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String homeName = StringArgumentType.getString(ctx, "name");
                            if (player == null) return 0;

                            PlayerStorage playerStorage = EEssentials.storage.getPlayerStorage(player);

                            Location location = playerStorage.homes.remove(homeName);
                            playerStorage.save();

                            if (location != null) {
                                player.sendMessage(Text.literal("Home " + homeName + " has been removed."), false);
                                return 1;
                            } else {
                                player.sendMessage(Text.literal("Home " + homeName + " does not exist!"), false);
                                return 0;
                            }
                        })
                )
        );

        // Regular /home command
        dispatcher.register(literal("home")
                .requires(src -> Permissions.check(src, HOME_PERMISSION_NODE, 2))
                .executes(ctx -> listHomes(ctx)) // If just /home is executed
                .then(argument("name", StringArgumentType.word())
                        .suggests(HomeCommands::suggestHomes)
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String homeName = StringArgumentType.getString(ctx, "name");
                            return teleportToHome(ctx, player, homeName);
                        }))
        );

        // Command for teleporting to another player's home
        dispatcher.register(
                literal("home:")
                .requires(src -> Permissions.check(src, HOME_OTHER_PERMISSION_NODE, 2))
                .then(argument("target", StringArgumentType.word())
                        .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                        .then(argument("homeName", StringArgumentType.word())
                                .suggests(HomeCommands::suggestTargetHomes)
                                .executes(ctx -> {
                                    String targetName = StringArgumentType.getString(ctx, "target");
                                    String homeName = StringArgumentType.getString(ctx, "homeName");
                                    return teleportToTargetHome(ctx, targetName, homeName);
                                })
                        ))
        );
    }

    private static int listHomes(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        PlayerStorage playerStorage = EEssentials.storage.getPlayerStorage(player);
        if (playerStorage.homes.isEmpty()) {
            player.sendMessage(Text.literal("You have no homes set."), false);
        } else {
            player.sendMessage(Text.literal("Homes: " + String.join(", ", playerStorage.homes.keySet())));
        }

        return 1;
    }

    private static int teleportToHome(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, String homeName) {
        if (player == null) return 0;

        PlayerStorage playerStorage = EEssentials.storage.getPlayerStorage(player);
        Location location = playerStorage.homes.get(homeName);

        if (location != null) {
            location.teleport(player);
            player.sendMessage(Text.literal("Teleporting to " + homeName + "."), false);
            return 1;
        } else {
            player.sendMessage(Text.literal("Invalid Home. Please do `/home (name)`. To see all available homes, type in `/homes`."), false);
            return 0;
        }
    }
    public static CompletableFuture<Suggestions> suggestHomes(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return CommandSource.suggestMatching(new String[]{""}, builder);
        return CommandSource.suggestMatching(EEssentials.storage.getPlayerStorage(ctx.getSource().getPlayer().getUuid()).homes.keySet(), builder);
    }

    private static int teleportToTargetHome(CommandContext<ServerCommandSource> ctx, String targetName, String homeName) {
        ServerCommandSource source = ctx.getSource();
        UUID targetUUID;
        GameProfile profile = getProfileForName(targetName);

        if (profile != null) {
            targetUUID = profile.getId();
        } else {
            source.sendMessage(Text.of("Unknown player: " + targetName));
            return 0;
        }

        PlayerStorage targetStorage = PlayerStorage.fromPlayerUUID(targetUUID);
        Location location = targetStorage.homes.get(homeName);

        if (location != null) {
            location.teleport(source.getPlayer());
            source.sendMessage(Text.of("Teleported to " + targetName + "'s home named " + homeName + "."));
            return 1;
        } else {
            source.sendMessage(Text.of(targetName + " does not have a home named " + homeName + "."));
            return 0;
        }
    }

    public static CompletableFuture<Suggestions> suggestTargetHomes(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        String targetName = StringArgumentType.getString(ctx, "target");
        GameProfile profile = getProfileForName(targetName);

        if (profile == null || profile.getId() == null) {
            return CommandSource.suggestMatching(new String[]{}, builder);
        }

        UUID targetUUID = profile.getId();
        return CommandSource.suggestMatching(PlayerStorage.fromPlayerUUID(targetUUID).homes.keySet(), builder);
    }

    private static GameProfile getProfileForName(String name) {
        File[] files = StorageManager.playerStorageDirectory.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".json")) {
                    PlayerStorage storage = PlayerStorage.fromPlayerUUID(UUID.fromString(file.getName().replace(".json", "")));
                    if (storage != null && name.equals(storage.getPlayerName())) {
                        return new GameProfile(storage.getPlayerUUID(), name);
                    }
                }
            }
        }
        return null;
    }

}
