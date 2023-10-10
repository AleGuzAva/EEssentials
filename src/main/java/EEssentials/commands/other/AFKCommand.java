package EEssentials.commands.other;

import EEssentials.util.AFKManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import static net.minecraft.server.command.CommandManager.*;

/**
 * Provides command to toggle AFK status for the player.
 */
public class AFKCommand {

    // Permission node for the afk command.
    public static final String AFK_PERMISSION_NODE = "eessentials.afk";

    /**
     * Registers the AFK command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("afk")
                        .requires(Permissions.require(AFK_PERMISSION_NODE, 2))
                        .executes(ctx -> toggleAFKStatus(ctx))
        );
    }

    /**
     * Toggles the AFK status of the player and notifies the server.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    private static int toggleAFKStatus(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) return 0;

        AFKManager.toggleAFK(player);

        return 1;
    }


}
