package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.Map;
import java.util.Optional;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.mvn.ext.ApplicablePlugins;

public class DependencyResolver {

    private final Configuration configuration;

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
        for (final String strategy : strategies) {
            final Dependency dependency = dependencies.get(strategy);
            model.addDependency(dependency);
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
        smartTestingSurefireProvider.setArtifactId("smart-testing-surefire-provider");
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
