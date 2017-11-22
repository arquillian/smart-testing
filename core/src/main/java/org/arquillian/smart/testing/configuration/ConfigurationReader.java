package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.yaml.snakeyaml.Yaml;

import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

class ConfigurationReader {

    private static final Logger logger = Log.getLogger();

    Map<String, Object> readConfiguration(File configPath) {
        if (!configPath.isDirectory()) {
            return getConfigParametersFromFile(getConfigurationFilePath(configPath));
        }

        final File[] files =
            configPath.listFiles((dir, name) -> name.equals(SMART_TESTING_YML) || name.equals(SMART_TESTING_YAML));

        if (files == null) {
            throw new RuntimeException("I/O errors occurs while listing dir " + configPath);
        }

        if (files.length == 0) {
            logger.info("Config file `" + SMART_TESTING_YAML + "` OR `" + SMART_TESTING_YML + "` is not found. "
                + "Using system properties to load configuration for smart testing.");
        } else {
            return getConfigParametersFromFile(getConfigurationFilePath(files));
        }
        return Collections.emptyMap();
    }

    Map<String, Object> getConfigParametersFromFile(Path filePath) {
        if (!filePath.toFile().exists()) {
            logger.warn(String.format("The configuration file %s is not exists.", filePath));
            return Collections.emptyMap();
        }
        try (InputStream io = Files.newInputStream(filePath)) {
            final Yaml yaml = new Yaml();
            Map<String, Object> yamlConfig = yaml.load(io);
            if (yamlConfig == null) {
                logger.warn(String.format("The configuration file %s is empty.", filePath));
                return Collections.emptyMap();
            } else {
                return yamlConfig;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path getConfigurationFilePath(File... files) {
        Path configPath;
        if (files.length == 1) {
            configPath = files[0].toPath();
        } else {
            configPath = getDefaultConfigFile(files);
        }
        logger.info("Using configuration from " + configPath);
        return configPath;
    }

    private Path getDefaultConfigFile(File... files) {
        if (files.length == 2) {
            logger.warn(
                "Found multiple config files with supported names: " + SMART_TESTING_YAML + ", " + SMART_TESTING_YML);
        }

        return Arrays.stream(files)
            .filter(file -> {
                if (files.length == 2) {
                    return file.getName().equals(SMART_TESTING_YML);
                }
                return file.getName().equals(SMART_TESTING_YAML) || file.getName().equals(SMART_TESTING_YML);
            })
            .map(File::toPath)
            .findFirst()
            .get();
    }
}
