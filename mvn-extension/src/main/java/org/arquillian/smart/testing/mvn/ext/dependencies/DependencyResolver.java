package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.StringSimilarityCalculator;
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
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(configuration.getCustomStrategiesDefinition());
        model.addDependency(smartTestingProviderDependency());
        final Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();
        final List<String> strategyMismatch = new ArrayList<>();
        final List<String> registeredStrategies = new ArrayList<>();
        for (int i=0; i < strategies.length; i++) {
            String definedStrategy = strategies[i];
            if (!dependencies.containsKey(definedStrategy)) {
                final String closestMatch = stringSimilarityCalculator.findClosestMatch(definedStrategy, dependencies.keySet());
                if (configuration.isAutocorrect()) {
                    if (registeredStrategies.contains(closestMatch)) {
                        strategyMismatch.add(String.format("Autocorrected [%s] strategy to [%s] but it was already registered", closestMatch, definedStrategy));
                    } else {
                        final Dependency dependency = dependencies.get(closestMatch);
                        model.addDependency(dependency);
                        strategies[i]= closestMatch;
                        registeredStrategies.add(closestMatch);
                    }
                } else {
                    strategyMismatch.add(String.format("Unable to find strategy [%s]. Did you mean [%s]?", definedStrategy, closestMatch));
                }
            } else {

                if (registeredStrategies.contains(definedStrategy)) {
                    strategyMismatch.add(String.format("Strategy [%s] was already registered or autocorrected", definedStrategy));
                } else {
                    final Dependency dependency = dependencies.get(definedStrategy);
                    model.addDependency(dependency);
                    registeredStrategies.add(definedStrategy);
                }
            }
        }
        strategyMismatch.forEach(msg -> LOGGER.error(msg));
        if (!strategyMismatch.isEmpty()) {
            throw new IllegalStateException("Unknown strategies (see above). Please refer to http://arquillian.org/smart-testing/#_strategies "
                + "for the list of available strategies.");
        }
        configuration.setStrategies(strategies);
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
