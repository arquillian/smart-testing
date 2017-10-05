package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.maven.model.Dependency;
import org.arquillian.smart.testing.impl.StrategyDependencyResolver;

/**
 * Resolves dependencies for strategies defined by keywords (e.g. new, changed, affected)
 *
 * For example one can defined mapping between "awesome" strategy to its corresponding dependency as follows:
 *
 * smart.testing.strategy.awesome=org.awesome:smart-testing-awesome:0.0.1
 *
 * Version in the g:a:v can be omitted and will be resolved to the one matching "Smart Testing Extension" which is in use.
 *
 * Properties are loaded with the following precedence:
 *
 *  - default ones stored internally are loaded first
 *  - custom file can overwrite defaults
 *  - System properties overwrite all above
 */
class MavenStrategyDependencyResolver extends StrategyDependencyResolver {


    MavenStrategyDependencyResolver(Path propertiesPath) {
        super(propertiesPath);
    }

    MavenStrategyDependencyResolver() {
        super();
    }

    Map<String, Dependency> resolveDependencies() {
        final Properties properties = new Properties();
        properties.putAll(loadDefaultMapping());
        properties.putAll(loadFromFile());
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
