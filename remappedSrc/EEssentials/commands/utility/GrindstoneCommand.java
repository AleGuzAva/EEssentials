package EEssentials.commands.utility;

import EEssentials.screens.GrindstoneScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Provides command to open a virtual grindstone.
 */
public class GrindstoneCommand {

    // Permission node for the grindstone command.
    public static final String GRINDSTONE_PERMISSION_NODE = "eessentials.grindstone";

    /**
     * Registers the grindstone command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("grindstone")
                        .requires(Permissions.require(GRINDSTONE_PERMISSION_NODE, 2))
                        .executes(ctx -> openGrindstone(ctx))
        );
    }

    /**
     * Opens a virtual grindstone for the executing player.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    private static int openGrindstone(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (int syncId, net.minecraft.entity.player.PlayerInventory inventory, net.minecraft.entity.player.PlayerEntity playerEntity) ->
                        new GrindstoneScreen(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())),
                Text.translatable("container.grindstone_title")
        ));

        player.incrementStat(Stats.INTERACT_WITH_GRINDSTONE);
        return 1;
    }
}
