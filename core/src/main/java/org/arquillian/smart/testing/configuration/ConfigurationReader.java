package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.yaml.snakeyaml.Yaml;

import static org.arquillian.smart.testing.configuration.Configuration.INHERIT;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

class ConfigurationReader {

    private static final Logger logger = Log.getLogger();

    static Map<String, Object> readEffectiveConfiguration(File configPath) {
        if (!configPath.isDirectory()) {
            return readEffectiveConfig(getConfigurationFilePath(configPath));
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
            return readEffectiveConfig(getConfigurationFilePath(files));
        }
        return Collections.emptyMap();
    }

    private static Map<String, Object> readEffectiveConfig(Path filePath){
        Map<String, Object> config = getConfigParametersFromFile(filePath);
        Deque<Map<String, Object>> configs = new ArrayDeque<>();
        configs.add(config);
        while (config.get(INHERIT) != null) {
            String inherit = String.valueOf(config.get(INHERIT));
            filePath = filePath.getParent().resolve(inherit);
            config = getConfigParametersFromFile(filePath);
            if (!config.isEmpty()) {
                configs.addFirst(config);
            }
        }

        Map<String, Object> effectiveConfig = configs.pollFirst();
        while (!configs.isEmpty()) {
            overwriteInnerProperties(effectiveConfig, configs.pollFirst());
        }

        effectiveConfig.remove(INHERIT);

        return effectiveConfig;
    }

    private static void overwriteInnerProperties(Map<String, Object> effective, Map<String, Object> inner) {
        for (String key: inner.keySet()) {
            if (isNonTrivialPropertyContainedInMap(key, inner, effective)) {
                effective.put(key, inner.get(key));
            } else {
                final Map<String, Object> effectiveValue = ((Map<String, Object>) effective.get(key));
                final Map<String, Object> innerValue = ((Map<String, Object>) inner.get(key));
                overwriteInnerProperties(effectiveValue, innerValue);
                effective.put(key, effectiveValue);
            }
        }
    }

    private static boolean isNonTrivialPropertyContainedInMap(String key, Map<String, Object> inner,
        Map<String, Object> effective) {
        return !Map.class.isAssignableFrom(inner.get(key).getClass()) || !effective.containsKey(key);
    }

    private static Map<String, Object> getConfigParametersFromFile(Path filePath) {
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

    private static Path getConfigurationFilePath(File... files) {
        Path configPath;
        if (files.length == 1) {
            configPath = files[0].toPath();
        } else {
            configPath = getDefaultConfigFile(files);
        }
        logger.info("Using configuration from " + configPath);
        return configPath;
    }

    private static Path getDefaultConfigFile(File... files) {
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
