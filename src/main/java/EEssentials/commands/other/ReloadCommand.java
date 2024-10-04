package EEssentials.commands.other;

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
            // Reloads config, rtp, and lang.yml
            EEssentials.INSTANCE.configManager();

            // Reload Locations.json separately after the server is up and running
            if (EEssentials.storage.locationManager != null) {
                EEssentials.storage.locationManager.load();
                source.sendMessage(Text.literal("Configurations reloaded successfully."));
                EEssentials.LOGGER.info("Configurations and Locations.json reloaded successfully.");
            } else {
                source.sendMessage(Text.literal("Failed to reload Locations.json. LocationManager is null."));
                EEssentials.LOGGER.warn("Failed to reload Locations.json. LocationManager is null.");
            }
            return 1;
        } catch (Exception e) {
            source.sendMessage(Text.literal("Error occurred while reloading configurations."));
            EEssentials.LOGGER.error("Failed to reload configurations", e);
            return 0;
        }
    }
}
