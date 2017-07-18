package org.arquillian.smart.testing.mvn.ext;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.maven.model.Dependency;

public class StrategyDependencyResolver {

    private static final String SMART_TESTING_STRATEGY_PREFIX = "smart.testing.strategy.";

    private Path propertiesPath; // TODO this could be configurable through system property and with this we need a path

    private Properties properties;

    StrategyDependencyResolver(Path propertiesPath, Properties properties) {
        this.propertiesPath = propertiesPath;
        this.properties = properties;
    }

    StrategyDependencyResolver(Properties properties) {
        this(null, properties);
    }

    StrategyDependencyResolver(Path propertiesPath) {
        this(propertiesPath, new Properties());
    }

    StrategyDependencyResolver() {
        this(null, new Properties());
    }

    Map<String, Dependency> resolveDependencies() {
        final Properties properties = new Properties();
        properties.putAll(loadFromFile());
        properties.putAll(this.properties);
        properties.putAll(System.getProperties());
        return transformToDependencies(properties);
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
                    String version = ExtensionVersion.version();
                    if (gav.length == 3) {
                        version = gav[2];
                    }
                    dependency.setVersion(version);
                    dependency.setScope("runtime");
                    return dependency;
                }));
    }

    private Properties loadFromFile() {
        final Properties properties = new Properties();
        if (propertiesPath != null) {
            try {
                properties.load(new FileInputStream(this.propertiesPath.toFile()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return properties;
    }
}
