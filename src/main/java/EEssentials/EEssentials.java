package EEssentials;

import EEssentials.commands.other.*;
import EEssentials.commands.teleportation.*;
import EEssentials.commands.utility.*;
import EEssentials.config.Configuration;
import EEssentials.config.YamlConfiguration;
import EEssentials.lang.LangManager;
import EEssentials.settings.HatSettings;
import EEssentials.settings.randomteleport.RTPSettings;
import EEssentials.storage.PlayerStorage;
import EEssentials.storage.StorageManager;
import EEssentials.util.Location;
import EEssentials.util.AsynchronousUtil;
import EEssentials.util.TeleportUtil;
import EEssentials.util.AFKManager;
import EEssentials.util.PermissionHelper;
import EEssentials.util.importers.EssentialCommandsImporter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class for the EEssentials mod, responsible for mod initialization and other lifecycle events.
 */
public class EEssentials implements ModInitializer {

    // Logger instance for logging messages related to EEssentials.
    public static final Logger LOGGER = LoggerFactory.getLogger("EEssentials");

    // Storage manager instance for handling data storage for EEssentials.
    public static final StorageManager storage =
            new StorageManager(FabricLoader.getInstance().getConfigDir().resolve("EEssentials"));

    // Reference to the active Minecraft server instance.
    public static MinecraftServer server = null;

    // Singleton instance of the EEssentials mod for global access.
    public static PermissionHelper perms = null;

    // Singleton instance of the mod.
    public static final EEssentials INSTANCE = new EEssentials();

    // Counters for tracking ticks in the server. Used for various timed functionalities.
    private static int tickCounter = 0;
    private static int afkTickCounter = 0;

    // Config
    Configuration mainConfig = getConfig("config.yml");

    /**
     * Called during the mod initialization phase.
     * Handles registration of commands, event listeners, and other initial setup.
     */
    @Override
    public void onInitialize() {
        // Display an ASCII Art message in the log for branding.
        displayAsciiArt();
        LOGGER.info("EEssentials Loaded!");

        // Initialize configuration
        this.configManager();

        // Register all the commands available in the mod.
        registerCommands();

        // Execute tasks and listeners that should run when the server starts.
        registerServerStartListeners();

        // Register tick listeners to perform periodic checks or actions.
        registerTickListeners();

        // Register player connection event listeners.
        registerConnectionEventListeners();

        // Tells the asynchronous executor to shut down when the server does to not have hanging threads.
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> AsynchronousUtil.shutdown());
    }

    /**
     * Register all commands provided by the mod.
     */
    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Reload Command - Should not be allowed to be toggled
            ReloadCommand.register(dispatcher);

            AFKCommand.register(dispatcher);
            // Check if each command or command group is enabled before registering
            if (mainConfig.getBoolean("Commands.ascend", true)) {
                AscendCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.back", true)) {
                BackCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.time", true)) {
                CheckTimeCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.clearinventory", true)) {
                ClearInventoryCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.descend", true)) {
                DescendCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.disposal", true)) {
                DisposalCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.enderchest", true)) {
                EnderchestCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.feed", true)) {
                FeedCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.fly", true)) {
                FlyCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.home", true)) {
                HomeCommands.register(dispatcher); // includes /home, /homes, /sethome, /delhome
            }
            if (mainConfig.getBoolean("Commands.gm", true)) {
                GamemodeAliasesCommands.register(dispatcher); // includes /gma, /gmc, /gms, /gmsp
            }
            if (mainConfig.getBoolean("Commands.message", true)) {
                MessageCommands.register(dispatcher); // includes /msg, /reply
                SocialSpyCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.playtime", true)) {
                PlaytimeCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.rtp", true)) {
                RTPCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.seen", true)) {
                SeenCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.smite", true)) {
                SmiteCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.playtime", true)) {
                PlaytimeCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.rtp", true)) {
                RTPCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.seen", true)) {
                SeenCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.smite", true)) {
                SmiteCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.spawn", true)) {
                SpawnCommands.register(dispatcher); // includes /spawn, /setspawn
            }
            if (mainConfig.getBoolean("Commands.speed", true)) {
                SpeedCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.time", true)) {
                CheckTimeCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.top", true)) {
                TopCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.tp", true)) {
                TPHereCommand.register(dispatcher);
                TPOfflineCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.tpa", true)) {
                TPACommands.register(dispatcher); // includes /tpa, /tpahere, /tpaccept, /tpdeny, /tpacancel
            }
            if (mainConfig.getBoolean("Commands.warp", true)) {
                WarpCommands.register(dispatcher); //  includes /warp, /warps, /setwarp, /delwarp
            }
            if (mainConfig.getBoolean("Commands.workbench", true)) {
                WorkbenchCommand.register(dispatcher);
            }
            if (mainConfig.getBoolean("Commands.textCommands", true)) {
                List<String> allTextCommands = getTextCommands();
                for (String textCommand : allTextCommands) {
                    new TextCommand(textCommand, dispatcher);
                }
            }
        });
    }

    /**
     * Register listeners that should be executed when the server starts.
     */
    private void registerServerStartListeners() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setupPermissions();
            EEssentials.server = server;
            storage.serverStarted();

            // Read the EssentialCommands import toggle from the configuration
            boolean ECImportFlag = mainConfig.getBoolean("Importers.EssentialCommands", false);

            if (ECImportFlag && !storage.locationManager.modImports.contains("essential_commands")) {
                LOGGER.info("Importing World Data from Essential Commands...");
                EssentialCommandsImporter.loadEssentialCommandsWorldData();
                LOGGER.info("Imported World Data from Essential Commands.");
                storage.locationManager.modImports.add("essential_commands");
                storage.locationManager.save();
            } else {
                LOGGER.info("Importing from Essential Commands is disabled in the configuration.");
            }
        });
    }

    /**
     * Register tick listeners to handle timed functionalities like checking AFK statuses.
     */
    private void registerTickListeners() {
        // Register tick listener
        ServerTickEvents.START_SERVER_TICK.register((MinecraftServer server) -> {
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
                Location spawn = storage.locationManager.serverSpawn;
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
            // Attempt to get an instance of LuckPermsProvider, signaling that permissions have been set up.
            perms = new PermissionHelper();
            LOGGER.info("Permissions system initialized!");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize permissions system!", e);
        }
    }

    public void configManager() {


        Configuration rtpConfig = mainConfig.getSection("Random-Teleport");
        RTPSettings.reload(rtpConfig);

        TeleportUtil.setUnsafeBlocks(mainConfig.getStringList("Unsafe-Blocks"));
        TeleportUtil.setAirBlocks(mainConfig.getStringList("Air-Blocks"));

        HatSettings.reload(mainConfig.getSection("Hat"));

        Configuration langConfig = getConfig("lang.yml");
        LangManager.loadConfig(langConfig);
    }

    public List<String> getTextCommands() {
        File folder = null;
        try {
            File motdFile = getOrCreateConfigurationFile("text-commands/motd.txt");
            folder = motdFile.getParentFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File[] files = folder.listFiles();
        List<String> textCommands = new ArrayList<>();
        if(files == null) return textCommands;
        for(File textFile : files) {
            String fileName = textFile.getName();
            if(fileName.contains(".txt")) {
                textCommands.add(fileName.replace(".txt", ""));
            }
        }
        return textCommands;
    }

    public File getConfigFolder() {
        File configFolder = FabricLoader.getInstance().getConfigDir().resolve("EEssentials").toFile();
        if (!configFolder.exists()) configFolder.mkdirs();
        return configFolder;
    }

    public File getOrCreateConfigurationFile(String fileName) throws IOException {
        File configFolder = getConfigFolder();
        File configFile = new File(configFolder, fileName);

        // Ensure parent directories exist
        File parentDir = configFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // Create parent directories if they don't exist
        }

        // Create the file if it doesn't exist
        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile);
            Path path = Paths.get("eessentials", fileName);
            InputStream in = getClass().getClassLoader().getResourceAsStream(path.toString().replace("\\", "/"));
            in.transferTo(outputStream);
        }
        return configFile;
    }

    public Configuration getConfig(String fileName) {
        Configuration config = null;
        try {
            config = YamlConfiguration.loadConfiguration(getOrCreateConfigurationFile(fileName));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return config;
    }
}
