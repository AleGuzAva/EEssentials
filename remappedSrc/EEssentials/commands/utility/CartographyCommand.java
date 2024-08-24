package EEssentials.commands.utility;

import EEssentials.screens.CartographyScreen;
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
 * Provides command to open a virtual cartography table.
 */
public class CartographyCommand {

    // Permission node for the cartography table command.
    public static final String CARTOGRAPHY_PERMISSION_NODE = "eessentials.cartography";

    /**
     * Registers the cartography table command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("cartography")
                        .requires(Permissions.require(CARTOGRAPHY_PERMISSION_NODE, 2))
                        .executes(ctx -> openCartography(ctx))
        );
    }

    /**
     * Opens a virtual cartography table for the executing player.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    private static int openCartography(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (int syncId, net.minecraft.entity.player.PlayerInventory inventory, net.minecraft.entity.player.PlayerEntity playerEntity) ->
                        new CartographyScreen(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())),
                Text.translatable("container.cartography_table")
        ));

        player.incrementStat(Stats.INTERACT_WITH_CARTOGRAPHY_TABLE);
        return 1;
    }
}
