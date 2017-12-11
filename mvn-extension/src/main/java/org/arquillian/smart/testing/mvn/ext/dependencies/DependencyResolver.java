package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.hub.storage.local.LocalStorageDirectoryAction;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;

public class DependencyResolver {

    private static Logger LOGGER = Log.getLogger();
    private final Configuration configuration;


    public DependencyResolver(Configuration configuration) {
        this.configuration = configuration;
    }

    public void addRequiredDependencies(Model model) {
        addStrategies(model);
    }

    private void addStrategies(Model model) {

        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(configuration.getCustomStrategies());
        final Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();
        final List<String> errorMessages = new ArrayList<>();

        configuration.autocorrectStrategies(dependencies.keySet(), errorMessages);
        errorMessages.forEach(msg -> LOGGER.error(msg));
        if (!errorMessages.isEmpty()) {
            throw new IllegalStateException("Unknown strategies (see above). Please refer to http://arquillian.org/smart-testing/#_strategies "
                + "for the list of available strategies.");
        }
        final String[] strategies = configuration.getStrategies();
        Arrays.stream(strategies).forEach(strategy -> {
            final Dependency dependency = dependencies.get(strategy);
            model.addDependency(dependency);
        });
        configuration.loadStrategyConfigurations(strategies);
    }

    public void addAsPluginDependency(Plugin plugin) {
        plugin.addDependency(smartTestingProviderDependency());
    }

    public void removeAndRegisterFirstCustomProvider(Model model, Plugin plugin) {
        final List<SurefireProviderDependency> providerDeps = findSurefireProviderDependencies(plugin);
        if (providerDeps.isEmpty()) {
            return;
        }
        final LocalStorageDirectoryAction customProvidersDir =
            TemporaryInternalFiles.createCustomProvidersDirAction(model.getProjectDirectory(), plugin.getArtifactId());
        SurefireProviderDependency providerDep = providerDeps.get(0);
        try {
            customProvidersDir.createWithFile(providerDep.getGAV(), providerDep.getProviderClassName().getBytes());
            plugin.removeDependency(providerDep.getDependency());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<SurefireProviderDependency> findSurefireProviderDependencies(Plugin plugin) {
        SurefireProviderResolver surefireProviderResolver = new SurefireProviderResolver(configuration);
        return plugin.getDependencies()
            .stream()
            .map(surefireProviderResolver::createSurefireProviderDepIfMatches)
            .collect(Collectors.toList());
    }

    private Dependency smartTestingProviderDependency() {
        final Dependency smartTestingSurefireProvider = new Dependency();
        smartTestingSurefireProvider.setGroupId("org.arquillian.smart.testing");
        smartTestingSurefireProvider.setArtifactId("surefire-provider");
        smartTestingSurefireProvider.setVersion(ExtensionVersion.version().toString());
        smartTestingSurefireProvider.setScope("runtime");
        smartTestingSurefireProvider.setClassifier("shaded");
        return smartTestingSurefireProvider;
    }
}
