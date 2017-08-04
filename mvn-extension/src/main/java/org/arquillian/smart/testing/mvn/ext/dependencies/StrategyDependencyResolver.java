package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.maven.model.Dependency;

/**
 * Resolves dependencies for strategies defined by keywords (e.g. new, changed, affected)
 *
 *
 */
class StrategyDependencyResolver {

    private static final String SMART_TESTING_STRATEGY_PREFIX = "smart.testing.strategy.";

    private Path propertiesPath; // TODO this could be configurable through system property and with this we need a path

    StrategyDependencyResolver(Path propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    StrategyDependencyResolver() {
        this.propertiesPath = null;
    }

    Map<String, Dependency> resolveDependencies() {
        final Properties properties = new Properties();
        properties.putAll(loadDefaultMapping());
        properties.putAll(loadFromFile());
        properties.putAll(System.getProperties());
        return transformToDependencies(properties);
    }

    private Properties loadDefaultMapping() {
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

    private Properties loadFromFile() {
        final Properties properties = new Properties();
        if (propertiesPath != null) {
            try {
                properties.load(new FileInputStream(this.propertiesPath.toFile()));
            } catch (IOException e) {
                throw new RuntimeException("Unable to load custom startegy mapping", e);
            }
        }

        return properties;
    }

    private Map<String, Dependency> transformToDependencies(Properties properties) {
        return properties
            .stringPropertyNames()
            .stream()
            .filter(key -> key.startsWith(SMART_TESTING_STRATEGY_PREFIX))
            .map(String::valueOf)
            .collect(Collectors.toMap(key -> key.substring(key.lastIndexOf('.') + 1),
                key -> {
                    final String[] gav = ((String) properties.get(key)).split(":");
                    final Dependency dependency = new Dependency();
                    dependency.setGroupId(gav[0]);
                    dependency.setArtifactId(gav[1]);
                    String version = ExtensionVersion.version().toString();
                    if (gav.length == 3) {
                        version = gav[2];
                    }
                    dependency.setVersion(version);
                    dependency.setScope("runtime");
                    return dependency;
                }));
    }
}
