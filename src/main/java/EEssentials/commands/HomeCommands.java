package EEssentials.commands;

import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.*;

/**
 * Defines the /home related commands to manage player homes.
 */
public class HomeCommands {

    // A map storing each player's homes. Key is the UUID of the player, value is another map with home names as keys and locations as values.
    private static final Map<String, Map<String, Location>> playerHomes = new HashMap<>();

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

                            Map<String, Location> homes = playerHomes.getOrDefault(player.getUuidAsString(), new HashMap<>());
                            homes.put(homeName, new Location(player.getServerWorld(), player.getX(), player.getY(), player.getZ()));
                            playerHomes.put(player.getUuidAsString(), homes);

                            player.sendMessage(Text.literal("Home " + homeName + " has been set to the current location."), false);
                            return 1;
                        })
                )
        );

        // Delete a home for the player.
        dispatcher.register(literal("delhome")
                .then(argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            Map<String, Location> homes = playerHomes.getOrDefault(player.getUuidAsString(), new HashMap<>());
                            return CommandSource.suggestMatching(homes.keySet(), builder);
                        })
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String homeName = StringArgumentType.getString(ctx, "name");

                            Map<String, Location> homes = playerHomes.getOrDefault(player.getUuidAsString(), new HashMap<>());
                            if (homes.containsKey(homeName)) {
                                homes.remove(homeName);
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
                        .suggests((ctx, builder) -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            Map<String, Location> homes = playerHomes.getOrDefault(player.getUuidAsString(), new HashMap<>());
                            return CommandSource.suggestMatching(homes.keySet(), builder);
                        })
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String homeName = StringArgumentType.getString(ctx, "name");

                            Map<String, Location> homes = playerHomes.get(player.getUuidAsString());
                            if (homes != null && homes.containsKey(homeName)) {
                                homes.get(homeName).teleport(player);
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
                    Map<String, Location> homes = playerHomes.get(player.getUuidAsString());
                    if (homes != null && !homes.isEmpty()) {
                        String homeList = String.join(", ", homes.keySet());
                        player.sendMessage(Text.literal("Homes: " + homeList), false);
                    } else {
                        player.sendMessage(Text.literal("You have no homes set."), false);
                    }
                    return 1;
                })
        );
    }
}
