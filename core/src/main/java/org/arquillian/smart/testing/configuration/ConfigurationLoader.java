package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.yaml.snakeyaml.Yaml;

import static org.arquillian.smart.testing.configuration.ObjectMapper.mapToObject;

public class ConfigurationLoader {

    public static final String SMART_TESTING_YML = "smart-testing.yml";
    public static final String SMART_TESTING_YAML = "smart-testing.yaml";
    public static final String SMART_TESTING_CONFIG = "smart.testing.config";

    private static final Logger logger = Log.getLogger();

    public static Configuration load() {
        return load(Paths.get("").toAbsolutePath().toFile());
    }

    public static Configuration load(File projectDir) {
        File configFile = projectDir;

        final String customConfigFile = System.getProperty(SMART_TESTING_CONFIG);
        if (customConfigFile != null) {
            configFile = Paths.get(customConfigFile).toAbsolutePath().toFile();
            if (!isCustomConfigFileValid(configFile)) {
                configFile = projectDir;
            }
        }
        Map<String, Object> yamlConfiguration = readConfiguration(configFile);
        return parseConfiguration(yamlConfiguration);
    }

    private static Map<String, Object> readConfiguration(File projectDir) {
        if (!projectDir.isDirectory()) {
            return getConfigParametersFromFile(getConfigurationFilePath(projectDir));
        }

        final File[] files =
            projectDir.listFiles((dir, name) -> name.equals(SMART_TESTING_YML) || name.equals(SMART_TESTING_YAML));

        if (files == null) {
            throw new RuntimeException("I/O errors occurs while listing dir " + projectDir);
        }

        if (files.length == 0) {
            logger.info("Config file `" + SMART_TESTING_YAML + "` OR `" + SMART_TESTING_YML + "` is not found. "
                + "Using system properties to load configuration for smart testing.");
        } else {
            return getConfigParametersFromFile(getConfigurationFilePath(files));
        }
        return Collections.EMPTY_MAP;
    }

    private static Map<String, Object> getConfigParametersFromFile(Path filePath) {
        try (InputStream io = Files.newInputStream(filePath)) {
            final Yaml yaml = new Yaml();
            Map<String, Object> yamlConfig = yaml.load(io);
            if (yamlConfig == null) {
                logger.warn(String.format("The configuration file %s is empty.", filePath));
                return new HashMap<>();
            } else {
                return yamlConfig;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        try (InputStream io = Files.newInputStream(path)) {
            final Yaml yaml = new Yaml();
            Map<String, Object> yamlConfiguration = yaml.load(io);
            return parseConfiguration(yamlConfiguration);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path getConfigurationFilePath(File... files) {
        if (files.length == 1) {
            final File configFile = files[0];
            logger.info("Using configuration from " + configFile.getName());
            return configFile.toPath();
        }

        if (files.length == 2) {
            logger.warn(
                "Found multiple config files with supported names: " + SMART_TESTING_YAML + ", " + SMART_TESTING_YML);
        }

        final Path configFilePath = Arrays.stream(files)
            .filter(file -> {
                if (files.length == 2) {
                    return file.getName().equals(SMART_TESTING_YML);
                }
                return file.getName().equals(SMART_TESTING_YAML) || file.getName().equals(SMART_TESTING_YML);
            })
            .map(File::toPath)
            .findFirst()
            .get();

        logger.info("Using configuration from " + configFilePath);

        return configFilePath;
    }

    private static Configuration parseConfiguration(Map<String, Object> yamlConfiguration) {
        final Object strategiesConfiguration = yamlConfiguration.get("strategiesConfiguration");
        final Configuration configuration = mapToObject(Configuration.class, yamlConfiguration);
        if (strategiesConfiguration != null) {
            configuration.setStrategiesConfig((Map<String, Object>) strategiesConfiguration);
        }
        return configuration;
    }

    private static Boolean isCustomConfigFileValid(File customConfigFile) {
        if (!customConfigFile.exists()) {
            logger.warn("Config file `" + customConfigFile.getName() + "` is not found. "
                + "Checking for config file `" + SMART_TESTING_YAML + "` OR `" + SMART_TESTING_YML);
            return false;
        }
        if (customConfigFile.isDirectory()) {
            logger.warn(customConfigFile.getName()
                + " is a directory. Using the default configuration or please specify a `yaml` configuration file.");
            return false;
        }
        return true;
    }
}
