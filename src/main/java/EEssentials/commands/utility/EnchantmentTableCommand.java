package EEssentials.commands.utility;

import EEssentials.commands.AliasedCommand;
import EEssentials.screens.EnchantmentTableScreen;
import EEssentials.screens.LoomScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Provides command to open a virtual Enchantment Table.
 */
public class EnchantmentTableCommand {

    // Permission node for the Enchantment Table command.
    public static final String ENCHANTMENTTABLE_PERMISSION_NODE = "eessentials.enchantmenttable";

    /**
     * Registers the Enchantment Table command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Register /enchantmenttable, /etable
        new AliasedCommand() {
            @Override
            public LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher) {
                return dispatcher.register(CommandManager.literal("enchantmenttable")
                        .requires(Permissions.require(ENCHANTMENTTABLE_PERMISSION_NODE, 2))
                        .executes(ctx -> openEnchantmentTable(ctx)));
            }
            @Override
            public String[] getCommandAliases() {
                return new String[]{"etable"};
            }
        }.registerWithAliases(dispatcher);
    }

    /**
     * Opens a virtual Enchantment Table for the executing player.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    private static int openEnchantmentTable(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (int syncId, net.minecraft.entity.player.PlayerInventory inventory, net.minecraft.entity.player.PlayerEntity playerEntity) ->
                        new EnchantmentTableScreen(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())),
                Text.translatable("container.enchant")
        ));

        return 1;
    }
}
