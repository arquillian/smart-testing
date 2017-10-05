package org.arquillian.smart.testing.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class StrategyDependencyResolver {

    protected static final String SMART_TESTING_STRATEGY_PREFIX = "smart.testing.strategy.";

    private final Path propertiesPath; // TODO this could be configurable through system property and with this we need a path

    public StrategyDependencyResolver(Path propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    public StrategyDependencyResolver() {
        this.propertiesPath = null;
    }

    public List<String> resolveStrategies() {
        final Properties properties = new Properties();
        properties.putAll(loadDefaultMapping());
        properties.putAll(loadFromFile());
        properties.putAll(System.getProperties());
        return transformToStrategies(properties);
    }

    protected List<String> transformToStrategies(Properties properties) {
        return properties
            .stringPropertyNames()
            .stream()
            .filter(key -> key.startsWith(SMART_TESTING_STRATEGY_PREFIX))
            .map(String::valueOf)
            .map(key -> key.substring(key.lastIndexOf('.') + 1))
            .collect(Collectors.toList());
    }

    protected Properties loadDefaultMapping() {
        final Properties properties = new Properties();
        try (InputStream strategyMapping = getClass().getClassLoader().getResourceAsStream("strategies.properties")) {
            if (strategyMapping == null) {
                throw new IllegalStateException("Unable to load default strategy dependencies mapping.");
            }
            properties.load(strategyMapping);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load default strategy dependencies mapping.", e);
        }
        return properties;
    }

    protected Properties loadFromFile() {
        final Properties properties = new Properties();
        if (propertiesPath != null) {
            try {
                properties.load(new FileInputStream(this.propertiesPath.toFile()));
            } catch (IOException e) {
                throw new RuntimeException("Unable to load custom strategy mapping", e);
            }
        }

        return properties;
    }
}
