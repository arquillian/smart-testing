package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.yaml.snakeyaml.Yaml;

public class ConfigurationLoader {

    public static final String SMART_TESTING_YML = "smart-testing.yml";
    public static final String SMART_TESTING_YAML = "smart-testing.yaml";
    public static final String SMART_TESTING_CONFIG = "smart.testing.config";

    private static final Logger logger = Log.getLogger();

    public static Configuration load() {
        return load(Paths.get("").toAbsolutePath().toFile());
    }

    public static Configuration load(File projectDir) {
        final File configFile;
        final String customConfigFilePath = System.getProperty(SMART_TESTING_CONFIG);
        if (isCustomConfigFileValid(customConfigFilePath)) {
            configFile = Paths.get(customConfigFilePath).toAbsolutePath().toFile();
        } else {
            configFile = projectDir;
        }

       return loadInheritedConfiguration(configFile);
    }

    private static Configuration loadInheritedConfiguration(File configFile) {
        Path configFileDir = configFile.isFile()? configFile.getParentFile().toPath(): configFile.toPath();

        final ConfigurationReader configurationReader = new ConfigurationReader();
        final Map<String, Object> yamlConfiguration = configurationReader.readConfiguration(configFile);

        final Configuration configuration = ConfigurationLoader.loadAsConfiguration(yamlConfiguration);

        final ConfigurationInheriter inheriter = new ConfigurationInheriter();
        return inheriter.overWriteNotDefinedValuesFromInherit(configuration, configFileDir);
    }

    public static Configuration load(File projectDir, String... strategies) {
        final Configuration configuration = load(projectDir);
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

    // testing
    static Configuration load(Path path) {
        return loadInheritedConfiguration(path.toFile());
    }

    private static Configuration loadAsConfiguration(Map<String, Object> yamlConfiguration) {
        final Object strategiesConfiguration = yamlConfiguration.get("strategiesConfiguration");
        final ObjectMapper objectMapper = new ObjectMapper();
        final Configuration configuration = objectMapper.readValue(Configuration.class, yamlConfiguration);
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
