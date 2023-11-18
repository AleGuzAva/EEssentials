package EEssentials.commands.utility;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import static net.minecraft.server.command.CommandManager.literal;
import net.minecraft.text.Text;
import EEssentials.EEssentials;

/**
 * Provides command to reload the configuration files.
 */
public class ReloadCommand {

    // Permission node for the reload command.
    public static final String RELOAD_PERMISSION_NODE = "eessentials.reload";

    /**
     * Registers the reload command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("eessentials")
                        .then(literal("reload")
                                .requires(Permissions.require(RELOAD_PERMISSION_NODE, 2))
                                .executes(ctx -> reloadConfigurations(ctx))
                        )
        );
    }

    /**
     * Reloads the mod's configuration files.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    private static int reloadConfigurations(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        try {
            EEssentials.INSTANCE.configManager();
            source.sendMessage(Text.literal("Configurations reloaded successfully."));
            return 1;
        } catch (Exception e) {
            source.sendMessage(Text.literal("Error occurred while reloading configurations."));
            EEssentials.LOGGER.error("Failed to reload configurations", e);
            return 0;
        }
    }
}
