package org.arquillian.smart.testing.mvn.ext;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.mvn.ext.dependencies.DependencyResolver;
import org.arquillian.smart.testing.mvn.ext.dependencies.ExtensionVersion;
import org.arquillian.smart.testing.mvn.ext.dependencies.Version;

import static org.arquillian.smart.testing.mvn.ext.MavenPropertyResolver.isSkipITs;

class MavenProjectConfigurator {

    private static final Version MINIMUM_VERSION = Version.from("2.19.1");

    private static final Logger logger = Logger.getLogger();

    private final Configuration configuration;

    private final DependencyResolver dependencyResolver;

    MavenProjectConfigurator(Configuration configuration) {
        this.configuration = configuration;
        this.dependencyResolver = new DependencyResolver(configuration);
    }

    void configureTestRunner(Model model) {
        final List<Plugin> effectiveTestRunnerPluginConfigurations = getEffectivePlugins(model);

        if (!effectiveTestRunnerPluginConfigurations.isEmpty()) {
            logger.debug("Enabling Smart Testing %s for plugin %s in %s module", ExtensionVersion.version().toString(),
                effectiveTestRunnerPluginConfigurations.stream()
                    .map(Plugin::getArtifactId)
                    .collect(Collectors.toList()).toString(), model.getArtifactId());

            dependencyResolver.addRequiredDependencies(model);

            effectiveTestRunnerPluginConfigurations
                .forEach(dependencyResolver::addAsPluginDependency);
        }
    }

    private List<Plugin> getEffectivePlugins(Model model) {
        final List<Plugin> testRunnerPluginConfigurations = model.getBuild().getPlugins()
            .stream()
            .filter(plugin -> ApplicablePlugins.contains(plugin.getArtifactId()))
            .filter(plugin -> {
                Version version = Version.from(plugin.getVersion().trim());
                return version.isGreaterOrEqualThan(MINIMUM_VERSION);
            })
            .filter(plugin -> {
                if (configuration.isApplyToDefined()) {
                    return configuration.getApplyTo().equals(plugin.getArtifactId());
                }
                // If not set the plugin is usable
                return true;
            })
            .collect(Collectors.toList());

        if (areNotApplicableTestingPlugins(testRunnerPluginConfigurations) && isNotPomProject(model)) {
            failBecauseOfPluginVersionMismatch(model);
        }

        return testRunnerPluginConfigurations.stream()
            .filter(
                testRunnerPlugin -> !(testRunnerPlugin.getArtifactId().equals("maven-failsafe-plugin") && isSkipITs()))
            .collect(Collectors.toList());
    }

    private void failBecauseOfPluginVersionMismatch(Model model) {
        logger.error(
            "Smart testing must be used with any of %s plugins with minimum version %s. Please add or update one of the plugin in <plugins> section in your pom.xml",
            ApplicablePlugins.ARTIFACT_IDS_LIST, MINIMUM_VERSION);
        logCurrentPlugins(model);
        throw new IllegalStateException(
            String.format("Smart testing must be used with any of %s plugins with minimum version %s",
                ApplicablePlugins.ARTIFACT_IDS_LIST, MINIMUM_VERSION));
    }

    private boolean areNotApplicableTestingPlugins(List<Plugin> testRunnerPluginConfigurations) {
        return testRunnerPluginConfigurations.size() == 0;
    }

    private boolean isNotPomProject(Model model) {
        return !"pom".equals(model.getPackaging().trim());
    }

    private void logCurrentPlugins(Model model) {

        model.getBuild().getPlugins()
            .stream()
            .filter(plugin -> ApplicablePlugins.contains(plugin.getArtifactId()))
            .forEach(plugin -> logger.error("Current applicable plugin: %s:%s:%s", plugin.getGroupId(),
                plugin.getArtifactId(), plugin.getVersion()));
    }
}
