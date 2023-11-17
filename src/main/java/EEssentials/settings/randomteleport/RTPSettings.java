package EEssentials.settings.randomteleport;

import EEssentials.config.Configuration;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RTPSettings {
    private static final Map<String, RTPWorldSettings> worldSettings = new HashMap<>();

    private static int maxAttempts = 10;

    private static List<String> blacklistedBiomes;

    public static void reload(Configuration rtpConfig) {
        worldSettings.clear();
        maxAttempts = rtpConfig.getInt("Max-Attempts", 10);
        Configuration worldsConfig = rtpConfig.getSection("Worlds");
        Map<String, String> redirectedWorlds = new HashMap<>();
        for(String worldName : worldsConfig.getKeys()) {
            Configuration worldConfig = worldsConfig.getSection(worldName);
            if(worldConfig != null) {
                if(!worldConfig.contains("Redirect-To")) {
                    worldSettings.put(worldName, new RTPWorldSettings(worldName, worldConfig));
                } else redirectedWorlds.put(worldName, worldConfig.getString("Redirect-To"));
            }
        }
        for(Map.Entry<String, String> redirect : redirectedWorlds.entrySet()) {
            worldSettings.put(redirect.getKey(), worldSettings.get(redirect.getValue()));
        }
        blacklistedBiomes = rtpConfig.getStringList("Blacklisted-Biomes");
    }

    public static int getMaxAttempts() {
        return maxAttempts;
    }

    public static RTPWorldSettings getWorldSettings(ServerWorld world) {
        return worldSettings.get(world.getRegistryKey().getValue().toString());
    }

    public static boolean isBiomeBlacklisted(String biomeKey) {
        return blacklistedBiomes.contains(biomeKey);
    }
}
