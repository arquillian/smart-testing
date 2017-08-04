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
            .anyMatch(dep -> "org.apache.maven.surefire".equals(dep.getGroupId()) && "surefire-api".equals(
                dep.getArtifactId()));
        if (!alreadyContains) {
            final Optional<Plugin> surefirePlugin = model.getBuild().getPlugins().stream()
                .filter(plugin -> ApplicablePlugins.SUREFIRE.hasSameArtifactId(plugin.getArtifactId()))
                .findFirst();

            surefirePlugin.ifPresent(plugin -> model.addDependency(
                getSurefireApiDependency(Version.from(plugin.getVersion().trim()).toString())));
        }
    }

    private Dependency getSurefireApiDependency(String version) {
        final Dependency dependency = new Dependency();
        dependency.setGroupId("org.apache.maven.surefire");
        dependency.setArtifactId("surefire-api");
        dependency.setVersion(version);
        dependency.setScope("runtime");
        return dependency;
    }

    private Dependency smartTestingProviderDependency() {
        final Dependency smartTestingSurefireProvider = new Dependency();
        smartTestingSurefireProvider.setGroupId("org.arquillian.smart.testing");
        smartTestingSurefireProvider.setArtifactId("smart-testing-surefire-provider");
        smartTestingSurefireProvider.setVersion(ExtensionVersion.version().toString());
        smartTestingSurefireProvider.setScope("runtime");
        return smartTestingSurefireProvider;
    }
}
