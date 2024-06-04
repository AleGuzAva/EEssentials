package EEssentials.config;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ConfigVersionUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigVersionUpdater.class);
    private final Configuration mainConfig;
    private final Configuration langConfig;
    private final String currentVersion;

    public ConfigVersionUpdater(Configuration mainConfig, Configuration langConfig, String currentVersion) {
        this.mainConfig = mainConfig;
        this.langConfig = langConfig;
        this.currentVersion = currentVersion;
    }

    public void updateConfig() {
        String configVersion = mainConfig.getString("Config-Version", "1.0.0");

        if (!configVersion.equals(currentVersion)) {
            LOGGER.info("Updating config from version " + configVersion + " to " + currentVersion);

            Configuration defaultConfig = YamlConfiguration.loadConfiguration(
                    getClass().getClassLoader().getResourceAsStream("eessentials/config.yml"));
            mergeConfigs(mainConfig, defaultConfig);

            mainConfig.set("Config-Version", currentVersion);

            try {
                // Save the config with comments
                saveConfigWithComments(mainConfig, "eessentials/config.yml", new File("config/EEssentials/config.yml"));
            } catch (IOException e) {
                LOGGER.error("Failed to save updated config!", e);
            }
        }

        String langVersion = langConfig.getString("Config-Version", "1.0.0");

        if (!langVersion.equals(currentVersion)) {
            LOGGER.info("Updating lang config from version " + langVersion + " to " + currentVersion);

            Configuration defaultLangConfig = YamlConfiguration.loadConfiguration(
                    getClass().getClassLoader().getResourceAsStream("eessentials/lang.yml"));
            mergeConfigs(langConfig, defaultLangConfig);

            langConfig.set("Config-Version", currentVersion);

            try {
                // Save the lang config with comments
                saveConfigWithComments(langConfig, "eessentials/lang.yml", new File("config/EEssentials/lang.yml"));
            } catch (IOException e) {
                LOGGER.error("Failed to save updated lang config!", e);
            }
        }
    }

    private void mergeConfigs(Configuration target, Configuration source) {
        for (String key : source.getKeys()) {
            if (source.get(key) instanceof Configuration) {
                if (!(target.get(key) instanceof Configuration)) {
                    target.set(key, new Configuration());
                }
                mergeConfigs(target.getSection(key), source.getSection(key));
            } else {
                if (!target.contains(key)) {
                    target.set(key, source.get(key));
                }
            }
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

    private void saveConfigWithComments(Configuration config, String resourcePath, File file) throws IOException {
        // Load the default configuration from the resource file
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        String defaultConfigContent = new String(resourceStream.readAllBytes(), Charsets.UTF_8);

        // Save the configuration to the file
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            writer.write(defaultConfigContent);
        }
    }
}
