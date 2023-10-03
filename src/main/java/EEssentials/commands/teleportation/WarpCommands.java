package EEssentials.commands.teleportation;

import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.*;

/**
 * Defines the /warp related commands to manage global warps.
 */
public class WarpCommands {

    // A map storing global warps with warp names as keys and locations as values.
    private static final Map<String, Location> warps = new HashMap<>();

    /**
     * Registers warp related commands (/setwarp, /delwarp, /warp, /warps).
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Set a new warp or overwrite an existing one.
        dispatcher.register(literal("setwarp")
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String warpName = StringArgumentType.getString(ctx, "name");

                            warps.put(warpName, new Location(player.getServerWorld(), player.getX(), player.getY(), player.getZ()));
                            player.sendMessage(Text.literal("Warp " + warpName + " set!"), false);
                            return 1;
                        })
                )
        );

        // Delete a warp.
        dispatcher.register(literal("delwarp")
                .then(argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> { // Add suggestion provider for easier user experience
                            return CommandSource.suggestMatching(warps.keySet(), builder);
                        })
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String warpName = StringArgumentType.getString(ctx, "name");

                            if (warps.containsKey(warpName)) {
                                warps.remove(warpName);
                                player.sendMessage(Text.literal("Warp " + warpName + " deleted!"), false);
                                return 1;
                            } else {
                                player.sendMessage(Text.literal("Warp " + warpName + " does not exist!"), false);
                                return 0; // Return a failure code.
                            }
                        })
                )
        );


        // Teleport the player to a specified warp.
// ...

// Teleport the player to a specified warp.
        dispatcher.register(literal("warp")
                .then(argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> { // Add suggestion provider for easier user experience
                            return CommandSource.suggestMatching(warps.keySet(), builder);
                        })
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String warpName = StringArgumentType.getString(ctx, "name");

                            if (warps.containsKey(warpName)) {
                                warps.get(warpName).teleport(player);
                                player.sendMessage(Text.literal("Teleported to warp " + warpName + "!"), false);
                                return 1;
                            } else {
                                player.sendMessage(Text.literal("Invalid Warp. Please do `/warp (name)`. To see all available warps, type in `/warps`."), false);
                                return 0; // Return a failure code.
                            }
                        })
                )
                .executes(ctx -> { // Default behavior if no argument is provided
                    ctx.getSource().sendError(Text.literal("Invalid Warp. Please do `/warp (name)`. To see all available warps, type in `/warps`."));
                    return 0; // Return a failure code.
                })
        );

        // List all global warps.
        dispatcher.register(literal("warps")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();

                    if (!warps.isEmpty()) {
                        String warpList = String.join(", ", warps.keySet());
                        player.sendMessage(Text.literal("Available warps: " + warpList), false);
                    } else {
                        player.sendMessage(Text.literal("No warps are currently set."), false);
                    }

                    return 1;
                })
        );
    }
}
