package EEssentials;

import EEssentials.commands.other.MessageCommands;
import EEssentials.commands.other.PlaytimeCommand;
import EEssentials.commands.other.SocialSpyCommand;
import EEssentials.commands.teleportation.*;
import EEssentials.commands.utility.*;
import EEssentials.events.ServerTickCallback;
import EEssentials.storage.StorageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class for the EEssentials mod, responsible for mod initialization and setup.
 */
public class EEssentials implements ModInitializer {

    // Logger instance for logging messages related to EEssentials.
    public static final Logger LOGGER = LoggerFactory.getLogger("EEssentials");
    public static final StorageManager storage =
            new StorageManager(FabricLoader.getInstance().getConfigDir().resolve("EEsentials"));

    public static MinecraftServer server = null;
    // Singleton instance of the mod.
    public static final EEssentials INSTANCE = new EEssentials();
    // Add a tick counter
    private static int tickCounter = 0;

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
            WarpCommands.register(dispatcher);
            SpawnCommands.register(dispatcher);
            TopCommand.register(dispatcher);
            ClearInventoryCommand.register(dispatcher);
            FeedCommand.register(dispatcher);
            HealCommand.register(dispatcher);
            PlaytimeCommand.register(dispatcher);
            EnderchestCommand.register(dispatcher);
            DisposalCommand.register(dispatcher);
            MessageCommands.register(dispatcher);
            SocialSpyCommand.register(dispatcher);
            FlyCommand.register(dispatcher);
            WorkbenchCommand.register(dispatcher);
        });

        // Perform additional setup (e.g., permissions) when the server starts.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setupPermissions();
            this.server = server;
            storage.serverStarted();
        });

        // Register tick listener
        ServerTickCallback.EVENT.register(() -> {
            tickCounter++;
            if (tickCounter >= 200) { // Every 10 seconds (200 ticks)
                TPACommands.checkForExpiredRequests();
                tickCounter = 0; // Reset the counter
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> {
            storage.playerLeft(handler.player);
        });

        ServerPlayConnectionEvents.JOIN.register((ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) -> {
            storage.playerJoined(handler.player);
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
