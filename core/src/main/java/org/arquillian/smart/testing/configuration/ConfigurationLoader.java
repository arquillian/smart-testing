package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.yaml.snakeyaml.Yaml;

import static org.arquillian.smart.testing.configuration.ConfigurationReader.readEffectiveConfiguration;
import static org.arquillian.smart.testing.configuration.ObjectMapper.mapToObject;

public class ConfigurationLoader {

    public static final String SMART_TESTING_YML = "smart-testing.yml";
    public static final String SMART_TESTING_YAML = "smart-testing.yaml";
    public static final String SMART_TESTING_CONFIG = "smart.testing.config";

    private static final Logger logger = Log.getLogger();

    /**
     * Loads a configuration from a config file in the given directory or from a file set by {@link SMART_TESTING_CONFIG}
     * system property. If the file is not present there, then it loads the default configuration.
     *
     * @param projectDir Directory the configuration file should be located in
     * @return An instance of {@link Configuration}
     */
    public static Configuration load(File projectDir) {
        return load(projectDir, null);
    }

    /**
     * Loads a configuration from a config file in the given directory or from a file set by {@link SMART_TESTING_CONFIG}
     * system property. If the file is not present in the given directory, and the stop condition is provided, then it
     * starts looking for the config file in all parent directories until the stop condition returns true.
     *
     * @param projectDir Directory the configuration file should be located in or the lookup should start from
     * @param stopCondition Should return true for a directory where the loader should stop looking for a configuration file
     * @return An instance of {@link Configuration}
     */
    public static Configuration load(File projectDir, Function<File, Boolean> stopCondition) {
        final File configFile;
        final String customConfigFilePath = System.getProperty(SMART_TESTING_CONFIG);

        if (isCustomConfigFileValid(customConfigFilePath)) {
            configFile = Paths.get(customConfigFilePath).toAbsolutePath().toFile();
        } else {
            if (stopCondition != null) {
                projectDir = new ConfigLookup(projectDir, stopCondition).getFirstDirWithConfigOrWithStopCondition();
            }
            configFile = projectDir;
        }

        return loadEffectiveConfiguration(configFile);
    }

    private static Configuration loadEffectiveConfiguration(File configFile) {
        final Map<String, Object> effectiveConfig = readEffectiveConfiguration(configFile);

        return loadAsConfiguration(effectiveConfig);
    }

    /**
     * Loads a configuration from a config file in the given directory or from a file set by {@link SMART_TESTING_CONFIG}
     * system property. If the file is not present in the given directory, and the stop condition is provided, then it
     * starts looking for the config file in all parent directories until the stop condition returns true.
     * When the configuration is loaded and any strategy is provided, then it loads also a configuration for the provided
     * strategies.
     *
     * @param projectDir Directory the configuration file should be located in or the lookup should start from
     * @param stopCondition Should return true for a directory where the loader should stop looking for a configuration file
     * @param strategies Strategies a configuration should be loaded for
     * @return An instance of {@link Configuration}
     */
    public static Configuration load(File projectDir, Function<File, Boolean> stopCondition, String... strategies) {
        final Configuration configuration = load(projectDir, stopCondition);
        configuration.loadStrategyConfigurations(strategies);

        return configuration;
    }

    public static Configuration loadPrecalculated(File projectDir) {
        final File configFile =
            new LocalStorage(projectDir).duringExecution().temporary().file(SMART_TESTING_YML).getFile();
        if (configFile.exists()) {
            return loadConfigurationFromFile(configFile);
        } else {
            return load(projectDir);
        }
    }

    static Configuration loadConfigurationFromFile(File configFile) {
        try (FileReader fileReader = new FileReader(configFile)) {
            final Yaml yaml = new Yaml();
            return yaml.loadAs(fileReader, Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration from file " + configFile, e);
        }
    }

    private static Configuration loadAsConfiguration(Map<String, Object> yamlConfiguration) {
        final Object strategiesConfiguration = yamlConfiguration.get("strategiesConfiguration");

        final Configuration configuration = mapToObject(Configuration.class, yamlConfiguration);
        if (strategiesConfiguration != null) {
            configuration.setStrategiesConfig((Map<String, Object>) strategiesConfiguration);
        }
        return configuration;
    }

    private static boolean isCustomConfigFileValid(String customConfigFilePath) {
        if (customConfigFilePath == null) {
            return false;
        }

        final File customConfigFile = Paths.get(customConfigFilePath).toAbsolutePath().toFile();
        if (!customConfigFile.exists()) {
            logger.warn("Config file `" + customConfigFile + "` is not found. "
                + "Using the default configuration file resolution.");
            return false;
        }

        if (customConfigFile.isDirectory()) {
            logger.warn(customConfigFile.getName()
                + " is a directory. Using the default configuration file resolution.");
            return false;
        }

        return true;
    }
}
