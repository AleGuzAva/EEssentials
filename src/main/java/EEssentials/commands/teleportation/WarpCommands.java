package EEssentials.commands.teleportation;

import EEssentials.EEssentials;
import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.*;

/**
 * Defines the /warp related commands to manage global warps.
 */
public class WarpCommands {

    // Permission Nodes for Warp Commands
    public static final String WARP_PERMISSION_NODE = "eessentials.warp.self";
    public static final String WARP_MANAGE_PERMISSION_NODE = "eessentials.warp.manage";

    public static final String WARP_LIST_PERMISSION_NODE = "eessentials.warp.list";

    /**
     * Registers warp related commands (/setwarp, /delwarp, /warp, /warps).
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Set a new warp or overwrite an existing one.
        dispatcher.register(literal("setwarp")
                .requires(Permissions.require(WARP_MANAGE_PERMISSION_NODE, 2))
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String warpName = StringArgumentType.getString(ctx, "name").toLowerCase();

                            // Use these values to create the new Location
                            Location warpLocation = new Location(player.getServerWorld(), player.getX(), player.getY(), player.getZ(), player.getPitch(), player.getYaw());

                            EEssentials.storage.locationManager.setWarp(warpName, warpLocation);
                            player.sendMessage(Text.literal("Warp " + warpName + " set!"), false);
                            return 1;
                        })
                )
        );


        // Delete a warp.
        dispatcher.register(literal("delwarp")
                .requires(Permissions.require(WARP_MANAGE_PERMISSION_NODE, 2))
                .then(argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            return CommandSource.suggestMatching(EEssentials.storage.locationManager.getWarpNames(), builder);
                        })
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String warpName = StringArgumentType.getString(ctx, "name").toLowerCase();

                            if (EEssentials.storage.locationManager.getWarp(warpName) != null) {
                                EEssentials.storage.locationManager.deleteWarp(warpName);
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
        dispatcher.register(literal("warp")
                .requires(Permissions.require(WARP_PERMISSION_NODE, 2))
                .then(argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            return CommandSource.suggestMatching(EEssentials.storage.locationManager.getWarpNames(), builder);
                        })
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String warpName = StringArgumentType.getString(ctx, "name").toLowerCase();

                            return teleportToWarp(player, warpName);
                        })
                )
                .executes(ctx -> {
                    ctx.getSource().sendError(Text.literal("Invalid Warp. Please do `/warp (name)`. To see all available warps, type in `/warps`."));
                    return 0;
                })
        );

        // List all global warps.
        dispatcher.register(literal("warps")
                .requires(Permissions.require(WARP_LIST_PERMISSION_NODE, 2))
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();

                    // Fetch warp names from the new storage system
                    Set<String> warpNames = EEssentials.storage.locationManager.getWarpNames();

                    if (!warpNames.isEmpty()) {
                        String warpList = String.join(", ", warpNames);
                        player.sendMessage(Text.literal("Available warps: " + warpList), false);
                    } else {
                        player.sendMessage(Text.literal("No warps are currently set."), false);
                    }

                    return 1;
                })
        );
    }

    private static int teleportToWarp(ServerPlayerEntity player, String warpName) {
        Location location = EEssentials.storage.locationManager.getWarp(warpName);
        if (location != null) {
            location.teleport(player);
            player.sendMessage(Text.literal("Teleported to warp " + warpName + "!"), false);
            return 1;
        } else {
            player.sendMessage(Text.literal("Invalid Warp. Please do `/warp (name)`. To see all available warps, type in `/warps`."), false);
            return 0;
        }
    }
}
