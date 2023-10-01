package EEssentials;

import EEssentials.commands.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class for the EEssentials mod, responsible for mod initialization and setup.
 */
public class EEssentials implements ModInitializer {

    // Logger instance for logging messages related to EEssentials.
    public static final Logger LOGGER = LoggerFactory.getLogger("EEssentials");

    // Singleton instance of the mod.
    public static final EEssentials INSTANCE = new EEssentials();

    /**
     * Called during mod initialization.
     */
    @Override
    public void onInitialize() {

        // Display an ASCII Art message in the log.
        displayAsciiArt();

        LOGGER.info("EEssentials Loaded!");

        // Register the mod's commands.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SpeedCommand.register(dispatcher);
            GamemodeAliasesCommands.register(dispatcher);
            TPACommands.register(dispatcher);
            HomeCommands.register(dispatcher);
        });

        // Perform additional setup (e.g., permissions) when the server starts.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setupPermissions();
        });
    }

    /**
     * Displays an ASCII Art representation of the mod's name in the log.
     */
    private void displayAsciiArt() {
        LOGGER.info("  ______ ______                    _   _       _     ");
        LOGGER.info(" |  ____|  ____|                  | | (_)     | |    ");
        LOGGER.info(" | |__  | |__   ___ ___  ___ _ __ | |_ _  __ _| |___ ");
        LOGGER.info(" |  __| |  __| / __/ __|/ _ \\ '_ \\| __| |/ _` | / __|");
        LOGGER.info(" | |____| |____\\__ \\__ \\  __/ | | | |_| | (_| | \\__ \\");
        LOGGER.info(" |______|______|___/___/\\___|_| |_|\\__|_|\\__,_|_|___/");
        LOGGER.info("                                                      ");
        LOGGER.info("                                                      ");
    }


    /**
     * Sets up permissions using the LuckPerms API.
     */
    private void setupPermissions() {
        try {
            // Attempt to get an instance of LuckPermsProvider, signaling that permissions have been set up.
            LuckPermsProvider.get();
            LOGGER.info("Permissions system initialized!");
        } catch (Exception e) {
            // Log an error if permissions initialization fails.
            LOGGER.error("Failed to initialize permissions system!", e);
        }
    }
}
