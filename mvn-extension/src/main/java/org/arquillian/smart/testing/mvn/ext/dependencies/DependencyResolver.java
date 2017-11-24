package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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


    public DependencyResolver(Configuration configuration) {
        this.configuration = configuration;
    }

    public void addRequiredDependencies(Model model) {
        addStrategies(model);
        addSurefireApiDependency(model);
    }

    private void addStrategies(Model model) {

        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(configuration.getCustomStrategies());
        model.addDependency(smartTestingProviderDependency());
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

    public Optional<Dependency> findJUnit5PlatformDependency(Plugin plugin) {
       return plugin.getDependencies()
            .stream()
            .filter(JUnit5SurefireProviderDependency::matches)
            .findFirst();
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

    static class JUnit5SurefireProviderDependency extends Dependency {
        private static final String GROUP_ID = "org.junit.platform";
        private static final String ARTIFACT_ID = "junit-platform-surefire-provider";

        JUnit5SurefireProviderDependency() {
            setGroupId(GROUP_ID);
            setArtifactId(ARTIFACT_ID);
        }

        public static boolean matches(Dependency dependency) {
            return GROUP_ID.equals(dependency.getGroupId()) && ARTIFACT_ID.equals(dependency.getArtifactId());
        }
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
