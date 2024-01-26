package EEssentials.commands.utility;

import EEssentials.lang.LangManager;
import EEssentials.settings.RepairSettings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class RepairCommand {

    // Permission node for the repair command.
    public static final String REPAIR_PERMISSION_NODE = "eessentials.repair";

    /**
     * Registers the repair command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("repair")
                        .requires(Permissions.require(REPAIR_PERMISSION_NODE, 2))
                        .executes(RepairCommand::repairItem)
        );
    }

    /**
     * Repairs the item in the player's hand.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    private static int repairItem(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player;

        try {
            player = source.getPlayer();
            if (player == null) {
                return 0;
            }

            ItemStack itemInHand = player.getMainHandStack();
            if (!itemInHand.isDamageable()) {
                LangManager.send(player, "Repair-Invalid");
                return 0;
            }

            if (RepairSettings.isBlacklisted(itemInHand)) {
                LangManager.send(player, "Repair-Invalid");
                return 0;
            }

            itemInHand.setDamage(0);
            LangManager.send(player, "Repair-Success");

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
