package EEssentials.settings.randomteleport;

import EEssentials.config.Configuration;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RTPSettings {
    private static final Map<String, RTPWorldSettings> worldSettings = new HashMap<>();
    private static int maxAttempts = 10;
    private static List<String> blacklistedBiomes;
    private static List<String> unsafeBlocks;
    private static List<String> airBlocks;

    public static void reload(Configuration rtpConfig, Configuration mainConfig) {
        System.out.println("Reloading RTP settings...");
        worldSettings.clear();
        maxAttempts = rtpConfig.getInt("Random-Teleport.Max-Attempts", 10);
        System.out.println("Max Attempts: " + maxAttempts);

        unsafeBlocks = mainConfig.getStringList("Unsafe-Blocks");
        airBlocks = mainConfig.getStringList("Air-Blocks");
        System.out.println("Loaded unsafe blocks: " + unsafeBlocks);
        System.out.println("Loaded air blocks: " + airBlocks);


        Configuration randomTeleportConfig = rtpConfig.getSection("Random-Teleport");
        if (randomTeleportConfig == null) {
            System.err.println("No 'Random-Teleport' section found in RTP config.");
            return;
        }

        Configuration worldsConfig = randomTeleportConfig.getSection("Worlds");
        if (worldsConfig == null) {
            System.err.println("No 'Worlds' section found in RTP config.");
            return;
        }

        System.out.println("Worlds section keys: " + worldsConfig.getKeys());

        Map<String, String> redirectedWorlds = new HashMap<>();
        for (String worldName : worldsConfig.getKeys()) {
            System.out.println("Loading config for world: " + worldName);
            Configuration worldConfig = worldsConfig.getSection(worldName);
            if (worldConfig != null) {
                if (!worldConfig.contains("Redirect-To")) {
                    worldSettings.put(worldName, new RTPWorldSettings(worldName, worldConfig));
                    System.out.println("Loaded settings for world: " + worldName);
                } else {
                    String redirectTo = worldConfig.getString("Redirect-To");
                    redirectedWorlds.put(worldName, redirectTo);
                    System.out.println("World " + worldName + " redirects to " + redirectTo);
                }
            }
        }

        for (Map.Entry<String, String> redirect : redirectedWorlds.entrySet()) {
            worldSettings.put(redirect.getKey(), worldSettings.get(redirect.getValue()));
            System.out.println("Applied redirection for world: " + redirect.getKey() + " to " + redirect.getValue());
        }

        System.out.println("World settings loaded: " + worldSettings.keySet());

        blacklistedBiomes = randomTeleportConfig.getStringList("Blacklisted-Biomes");
        System.out.println("Loaded blacklisted biomes: " + blacklistedBiomes);
    }


    public static int getMaxAttempts() {
        return maxAttempts;
    }

    public static RTPWorldSettings getWorldSettings(ServerWorld world) {
        String worldName = world.getRegistryKey().getValue().toString();
        return getWorldSettings(worldName);
    }

    public static RTPWorldSettings getWorldSettings(String worldName) {
        RTPWorldSettings settings = worldSettings.get(worldName);
        if (settings == null) {
            System.err.println("No settings found for world: " + worldName);
        }
        return settings;
    }


    public static boolean isBiomeBlacklisted(String biomeKey) {
        return blacklistedBiomes.contains(biomeKey);
    }

    public static Set<String> getAllWorlds() {
        return worldSettings.keySet();
    }
}
