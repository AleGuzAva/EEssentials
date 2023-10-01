package EEssentials.commands;

import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

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

                            // Retrieve or create the home map for the player.
                            Map<String, Location> homes = playerHomes.getOrDefault(player.getUuidAsString(), new HashMap<>());
                            homes.put(homeName, new Location(player.getServerWorld(), player.getX(), player.getY(), player.getZ()));
                            playerHomes.put(player.getUuidAsString(), homes);

                            player.sendMessage(Text.literal("Home " + homeName + " set!"), false);
                            return 1;
                        })
                )
        );

        // Delete a home for the player.
        dispatcher.register(literal("delhome")
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String homeName = StringArgumentType.getString(ctx, "name");

                            // Retrieve the home map for the player and remove the specified home.
                            Map<String, Location> homes = playerHomes.getOrDefault(player.getUuidAsString(), new HashMap<>());
                            if (homes.containsKey(homeName)) {
                                homes.remove(homeName);
                                player.sendMessage(Text.literal("Home " + homeName + " deleted!"), false);
                            } else {
                                player.sendMessage(Text.literal("Home " + homeName + " does not exist!"), false);
                            }

                            return 1;
                        })
                )
        );

        // Teleport the player to a specified home.
        dispatcher.register(literal("home")
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String homeName = StringArgumentType.getString(ctx, "name");

                            // Retrieve the home map for the player and teleport to the specified home.
                            Map<String, Location> homes = playerHomes.get(player.getUuidAsString());
                            if (homes != null && homes.containsKey(homeName)) {
                                homes.get(homeName).teleport(player);
                                player.sendMessage(Text.literal("Teleported to home " + homeName + "!"), false);
                            } else {
                                player.sendMessage(Text.literal("Home " + homeName + " does not exist!"), false);
                            }

                            return 1;
                        })
                )
        );

        // List all homes set by the player.
        dispatcher.register(literal("homes")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();

                    // Retrieve the home map for the player and list all homes.
                    Map<String, Location> homes = playerHomes.get(player.getUuidAsString());
                    if (homes != null && !homes.isEmpty()) {
                        String homeList = String.join(", ", homes.keySet());
                        player.sendMessage(Text.literal("Your homes: " + homeList), false);
                    } else {
                        player.sendMessage(Text.literal("You have no homes set."), false);
                    }

                    return 1;
                })
        );
    }
}
