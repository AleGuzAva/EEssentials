package EEssentials.commands.teleportation;

import EEssentials.EEssentials;
import EEssentials.storage.PlayerStorage;
import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Defines the /home related commands to manage player homes.
 */
public class HomeCommands {

    /**
     * Registers home related commands (/sethome, /delhome, /home, /homes).
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Set a new home or overwrite an existing one for the player.
        dispatcher.register(literal("sethome")
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

        // Teleport the player to a specified home.
        dispatcher.register(literal("home")
                .then(argument("name", StringArgumentType.word())
                        .suggests(HomeCommands::suggestHomes)
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String homeName = StringArgumentType.getString(ctx, "name");
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
                        })
                )
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) return 0;

                    PlayerStorage playerStorage = EEssentials.storage.getPlayerStorage(player);
                    if (playerStorage.homes.isEmpty()) {
                        player.sendMessage(Text.literal("You have no homes set."), false);
                    } else {
                        player.sendMessage(Text.literal("Homes: " + String.join(", ", playerStorage.homes.keySet())));
                    }

                    return 1;
                })
        );
    }

    public static CompletableFuture<Suggestions> suggestHomes(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return CommandSource.suggestMatching(new String[]{""}, builder);
        return CommandSource.suggestMatching(EEssentials.storage.getPlayerStorage(ctx.getSource().getPlayer().getUuid()).homes.keySet(), builder);
    }

}
