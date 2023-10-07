package EEssentials.commands.utility;


import EEssentials.screens.WorkbenchScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import static net.minecraft.server.command.CommandManager.*;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

/**
 * Provides command to open a virtual workbench.
 */
public class WorkbenchCommand {

    // Permission node for the workbench command.
    public static final String WORKBENCH_PERMISSION_NODE = "eessentials.workbench";

    /**
     * Registers the workbench command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("workbench")
                        .requires(Permissions.require(WORKBENCH_PERMISSION_NODE, 2))
                        .executes(ctx -> openWorkbench(ctx))
        );
    }

    /**
     * Opens a virtual workbench for the executing player.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    private static int openWorkbench(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity executingPlayer = source.getPlayer();

        if (executingPlayer == null) return 0;

        // Create a new screen handler factory for the custom crafting table
        executingPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> {
            return new WorkbenchScreen(i, playerInventory);
        }, Text.translatable("container.crafting")));

        executingPlayer.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);

        return 1;
    }

}
