package EEssentials.commands.utility;

import EEssentials.screens.WorkbenchScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import static net.minecraft.server.command.CommandManager.literal;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (int syncId, net.minecraft.entity.player.PlayerInventory inventory, net.minecraft.entity.player.PlayerEntity playerEntity) ->
                        new WorkbenchScreen(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())),
                Text.translatable("container.crafting")
        ));

        player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        return 1;
    }
}
