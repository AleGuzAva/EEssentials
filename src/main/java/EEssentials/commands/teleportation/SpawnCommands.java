package EEssentials.commands.teleportation;

import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.command.argument.EntityArgumentType;

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

        // Teleport the player or the target to the universal spawn location.
        dispatcher.register(literal("spawn")
                .executes(ctx -> teleportToSpawn(ctx)) // Teleport the executing player
                .then(argument("target", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                            return teleportToSpawn(ctx, target);  // Teleport the specified player
                        }))
        );
    }

    private static int teleportToSpawn(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = targets.length > 0 ? targets[0] : ctx.getSource().getPlayer();

        if (spawnLocation != null) {
            spawnLocation.teleport(player);

            if (player.equals(source.getPlayer())) {
                player.sendMessage(Text.of("Teleported to spawn."), false);
            } else {
                source.sendMessage(Text.of("Teleported " + player.getName().getString() + " to spawn."));
            }

            return 1;
        } else {
            player.sendMessage(Text.of("Spawn Location not found."), false);
            return 0; // Return a failure code.
        }
    }

}
