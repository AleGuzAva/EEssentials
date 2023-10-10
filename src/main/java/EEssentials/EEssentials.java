package EEssentials;

import EEssentials.commands.other.*;
import EEssentials.commands.teleportation.*;
import EEssentials.commands.utility.*;
import EEssentials.events.ServerTickCallback;
import EEssentials.storage.PlayerStorage;
import EEssentials.storage.StorageManager;
import EEssentials.util.Location;
import EEssentials.util.AFKManager;
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
 * The main class for the EEssentials mod, responsible for mod initialization and other lifecycle events.
 */
public class EEssentials implements ModInitializer {

    // Logger instance for logging messages related to EEssentials.
    public static final Logger LOGGER = LoggerFactory.getLogger("EEssentials");

    // Storage manager instance for handling data storage for EEssentials.
    public static final StorageManager storage =
            new StorageManager(FabricLoader.getInstance().getConfigDir().resolve("EEsentials"));

    // Reference to the active Minecraft server instance.
    public static MinecraftServer server = null;

    // Singleton instance of the EEssentials mod for global access.
    public static final EEssentials INSTANCE = new EEssentials();

    // Counters for tracking ticks in the server. Used for various timed functionalities.
    private static int tickCounter = 0;
    private static int afkTickCounter = 0;

    /**
     * Called during the mod initialization phase.
     * Handles registration of commands, event listeners, and other initial setup.
     */
    @Override
    public void onInitialize() {
        // Display an ASCII Art message in the log for branding.
        displayAsciiArt();
        LOGGER.info("EEssentials Loaded!");

        // Register all the commands available in the mod.
        registerCommands();

        // Execute tasks and listeners that should run when the server starts.
        registerServerStartListeners();

        // Register tick listeners to perform periodic checks or actions.
        registerTickListeners();

        // Register player connection event listeners.
        registerConnectionEventListeners();
    }

    /**
     * Register all commands provided by the mod.
     */
    private void registerCommands() {
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
            BackCommand.register(dispatcher);
            SeenCommand.register(dispatcher);
            TPOfflineCommand.register(dispatcher);
            CheckTimeCommand.register(dispatcher);
            AFKCommand.register(dispatcher);
            IgnoreCommands.register(dispatcher);
        });
    }

    /**
     * Register listeners that should be executed when the server starts.
     */
    private void registerServerStartListeners() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setupPermissions();
            this.server = server;
            storage.serverStarted();
        });
    }

    /**
     * Register tick listeners to handle timed functionalities like checking AFK statuses.
     */
    private void registerTickListeners() {
        ServerTickCallback.EVENT.register(() -> {
            tickCounter++;
            afkTickCounter++;

            // Check for expired teleportation requests every 200 ticks (10 seconds).
            if (tickCounter >= 200) {
                TPACommands.checkForExpiredRequests();
                tickCounter = 0; // Reset the counter
            }

            // Check player AFK statuses every 20 ticks (1 second).
            if (afkTickCounter >= 20) {
                AFKManager.checkAFKStatuses(server);
                afkTickCounter = 0; // Reset the AFK counter
            }
        });
    }

    /**
     * Register listeners for player connection events, like joining or leaving the server.
     */
    private void registerConnectionEventListeners() {
        // Actions to perform when a player disconnects from the server.
        ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> {
            PlayerStorage storage = EEssentials.storage.getPlayerStorage(handler.player);
            Location currentLogoutLocation = Location.fromPlayer(handler.player);
            storage.setLogoutLocation(currentLogoutLocation);
            storage.setLastTimeOnline();
            storage.save();
            EEssentials.storage.playerLeft(handler.player);

            // Reset AFK status and activity timer for the disconnecting player.
            AFKManager.setAFK(handler.player, false, false);
            AFKManager.resetActivity(handler.player);
        });

        // Actions to perform when a player joins the server.
        ServerPlayConnectionEvents.JOIN.register((ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) -> {
            storage.playerJoined(handler.player);
            PlayerStorage ps = storage.getPlayerStorage(handler.player);
            if (!ps.playedBefore) {
                Location spawn = storage.worldSpawns.serverSpawn;
                if (spawn != null) {
                    spawn.teleport(handler.player);
                }
            }

            // Reset AFK timers for the joining player.
            AFKManager.setAFK(handler.player, false, false);
            AFKManager.resetActivity(handler.player);
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
     * Initialize and setup permissions using the LuckPerms API.
     * This method ensures the permissions system is active and running.
     */
    private void setupPermissions() {
        try {
            LuckPermsProvider.get();
            LOGGER.info("Permissions system initialized!");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize permissions system!", e);
        }
    }

}
