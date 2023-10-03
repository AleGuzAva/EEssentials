package EEssentials.commands;

import EEssentials.util.PermissionHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import static net.minecraft.server.command.CommandManager.*;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Provides command to clear the player's inventory.
 */
public class ClearInventoryCommand {

    // Permission node for the clear inventory command.
    public static final String CLEAR_INVENTORY_PERMISSION_NODE = "eessentials.clearinventory";

    /**
     * Registers the clear inventory command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("clearinventory")
                        .requires(source -> hasPermission(source, CLEAR_INVENTORY_PERMISSION_NODE))
                        .executes(ctx -> clearInventory(ctx))  // Clears the inventory of the executing player
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return clearInventory(ctx, target);  // Clears the inventory of the specified player
                                }))
        );

        // CI is an alias for Clear Inventory
        dispatcher.register(
                literal("ci")
                        .requires(source -> hasPermission(source, CLEAR_INVENTORY_PERMISSION_NODE))
                        .executes(ctx -> clearInventory(ctx))  // Clears the inventory of the executing player
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return clearInventory(ctx, target);  // Clears the inventory of the specified player
                                }))
        );
    }

    /**
     * Clears the inventory of the target player.
     *
     * @param ctx The command context.
     * @param targets The target players.
     * @return 1 if successful, 0 otherwise.
     */
    private static int clearInventory(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = targets.length > 0 ? targets[0] : source.getPlayer();

        if (player == null) return 0;

        // Clearing main inventory
        player.getInventory().clear();

        // Clearing armor slots
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }

        // Clearing off-hand slot
        for (int i = 0; i < player.getInventory().offHand.size(); i++) {
            player.getInventory().offHand.set(i, ItemStack.EMPTY);
        }

        if (player.equals(source.getPlayer())) {
            player.sendMessage(Text.of("Cleared your inventory."), false);
        } else {
            source.sendMessage(Text.of("Cleared " + player.getName().getString() + "'s inventory."));
        }
        return 1;
    }

    /**
     * Checks if a player has the required permissions to execute a command.
     *
     * @param source The command source.
     * @param permissionNode The permission node for the command.
     * @return True if the player has permissions, false otherwise.
     */
    private static boolean hasPermission(ServerCommandSource source, String permissionNode) {
        return source.hasPermissionLevel(2) || PermissionHelper.hasPermission(source.getPlayer(), permissionNode);
    }
}
