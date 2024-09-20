package EEssentials.commands.utility;

import EEssentials.lang.LangManager;
import EEssentials.screens.DisposalScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class DisposalCommand {

    // Permission node for the disposal command.
    public static final String DISPOSAL_PERMISSION_NODE = "eessentials.disposal";

    /**
     * Registers the disposal command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("disposal")
                        .requires(Permissions.require(DISPOSAL_PERMISSION_NODE, 2))
                        .executes(DisposalCommand::openDisposal)
        );

        dispatcher.register(
                literal("trash")
                        .requires(Permissions.require(DISPOSAL_PERMISSION_NODE, 2))
                        .executes(DisposalCommand::openDisposal)
        );
    }

    private static int openDisposal(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        LangManager.send(player, "Disposal");
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, pl) -> new DisposalScreen(syncId, inventory), Text.of("Disposal"))); // Set the GUI title to "Disposal"
        return 1;
    }
}
