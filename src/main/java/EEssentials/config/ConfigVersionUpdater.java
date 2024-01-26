package EEssentials.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ConfigVersionUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigVersionUpdater.class);
    private final Configuration mainConfig;
    private final String currentVersion;

    public ConfigVersionUpdater(Configuration mainConfig, String currentVersion) {
        this.mainConfig = mainConfig;
        this.currentVersion = currentVersion;
    }

    public void updateConfig() {
        String configVersion = mainConfig.getString("Config-Version", "1.0.0");

        if (!configVersion.equals(currentVersion)) {
            LOGGER.info("Updating config from version " + configVersion + " to " + currentVersion);

            updateToLatest(configVersion, currentVersion);

            mainConfig.set("Config-Version", currentVersion);

            try {
                YamlConfiguration.save(mainConfig, new File("config/EEssentials/config.yml"));
            } catch (IOException e) {
                LOGGER.error("Failed to save updated config!", e);
            }
        }
    }

    private void updateToLatest(String oldVersion, String newVersion) {
        if (isOlderVersion(oldVersion, "1.0.1")) {
            addMissingKeysFor1_0_1();
        }
    }

    private void addMissingKeysFor1_0_1() {
        if (!mainConfig.contains("Commands.anvil")) {
            mainConfig.set("Commands.anvil", true);
        }
        if (!mainConfig.contains("Commands.grindstone")) {
            mainConfig.set("Commands.grindstone", true);
        }
        if (!mainConfig.contains("Commands.smithing")) {
            mainConfig.set("Commands.smithing", true);
        }
        if (!mainConfig.contains("Commands.stonecutter")) {
            mainConfig.set("Commands.stonecutter", true);
        }
    }

    private boolean isOlderVersion(String currentVersion, String targetVersion) {
        int[] currentParts = parseVersion(currentVersion);
        int[] targetParts = parseVersion(targetVersion);

        for (int i = 0; i < currentParts.length; i++) {
            if (currentParts[i] < targetParts[i]) {
                return true;
            } else if (currentParts[i] > targetParts[i]) {
                return false;
            }
        }
        return false;
    }

    private int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] numbers = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            numbers[i] = Integer.parseInt(parts[i]);
        }
        return numbers;
    }
}
