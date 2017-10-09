package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.mvn.ext.ApplicablePlugins;

public class DependencyResolver {

    private static Logger LOGGER = Log.getLogger();
    private final Configuration configuration;
    private final StringSimilarityCalculator stringSimilarityCalculator = new StringSimilarityCalculator();

    public DependencyResolver(Configuration configuration) {
        this.configuration = configuration;
    }

    public void addRequiredDependencies(Model model) {
        addStrategies(model);
        addSurefireApiDependency(model);
    }

    private void addStrategies(Model model) {
        final String[] strategies = configuration.getStrategies();
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver();
        model.addDependency(smartTestingProviderDependency());
        final Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();
        final Map<String, String> strategyMismatch = new HashMap<>();
        for (final String definedStrategy : strategies) {
            if (!dependencies.containsKey(definedStrategy)) {
                final String closestMatch = stringSimilarityCalculator.findClosestMatch(definedStrategy, dependencies.keySet());
                strategyMismatch.put(definedStrategy, closestMatch);
            } else {
                final Dependency dependency = dependencies.get(definedStrategy);
                model.addDependency(dependency);
            }
        }
        strategyMismatch.forEach((selection, match) -> LOGGER.error("Unable to find strategy [%s]. Did you mean [%s]?", selection, match));
        if (!strategyMismatch.isEmpty()) {
            throw new IllegalStateException("Unknown strategies (see above). Please refer to http://arquillian.org/smart-testing/#_strategies "
                + "for the list of available strategies.");
        }
    }

    public void addAsPluginDependency(Plugin plugin) {
        plugin.addDependency(smartTestingProviderDependency());
    }

    private void addSurefireApiDependency(Model model) {
        boolean alreadyContains = model.getDependencies()
            .stream()
            .anyMatch(SurefireApiDependency::matches);
        if (!alreadyContains) {
            final Optional<Plugin> surefirePlugin = model.getBuild().getPlugins().stream()
                .filter(plugin -> ApplicablePlugins.SUREFIRE.hasSameArtifactId(plugin.getArtifactId()))
                .findFirst();

            surefirePlugin.ifPresent(plugin -> model.addDependency(
                new SurefireApiDependency(Version.from(plugin.getVersion().trim()).toString())));
        }
    }

    private Dependency smartTestingProviderDependency() {
        final Dependency smartTestingSurefireProvider = new Dependency();
        smartTestingSurefireProvider.setGroupId("org.arquillian.smart.testing");
        smartTestingSurefireProvider.setArtifactId("surefire-provider");
        smartTestingSurefireProvider.setVersion(ExtensionVersion.version().toString());
        smartTestingSurefireProvider.setScope("runtime");
        return smartTestingSurefireProvider;
    }

    static class SurefireApiDependency extends Dependency {
        private static final String GROUP_ID = "org.apache.maven.surefire";
        private static final String ARTIFACT_ID = "surefire-api";

        SurefireApiDependency(String version) {
            setGroupId(GROUP_ID);
            setArtifactId(ARTIFACT_ID);
            setVersion(version);
            setScope("runtime");
        }

        public static boolean matches(Dependency dependency) {
            return GROUP_ID.equals(dependency.getGroupId()) && ARTIFACT_ID.equals(dependency.getArtifactId());
        }
    }
}
