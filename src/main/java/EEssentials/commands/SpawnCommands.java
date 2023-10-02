package EEssentials.commands;

import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.*;

/**
 * Defines the /spawn related commands to manage a universal spawn location.
 */
public class SpawnCommands {

    // A variable to store the universal spawn location.
    private static Location spawnLocation = null;

    /**
     * Registers spawn related commands (/setspawn, /spawn).
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Set a new universal spawn location.
        dispatcher.register(literal("setspawn")
                .requires(src -> src.hasPermissionLevel(2)) // Only operators can set the spawn
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();

                    spawnLocation = new Location(player.getServerWorld(), player.getX(), player.getY(), player.getZ());
                    player.sendMessage(Text.literal("Set Spawn to current location."), false);

                    return 1;
                })
        );

        // Teleport the player to the universal spawn location.
        dispatcher.register(literal("spawn")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();

                    if (spawnLocation != null) {
                        spawnLocation.teleport(player);
                        player.sendMessage(Text.literal("Teleporting to spawn."), false);
                        return 1;
                    } else {
                        player.sendMessage(Text.literal("Spawn Location not found."), false);
                        return 0; // Return a failure code.
                    }
                })
        );
    }
}
