package org.arquillian.smart.testing.mvn.ext;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.mvn.ext.checker.SkipModuleChecker;
import org.arquillian.smart.testing.mvn.ext.dependencies.DependencyResolver;
import org.arquillian.smart.testing.mvn.ext.dependencies.ExtensionVersion;
import org.arquillian.smart.testing.mvn.ext.dependencies.Version;

class MavenProjectConfigurator {

    private static final Version MINIMUM_VERSION = Version.from("2.19.1");

    private static final Logger logger = Log.getLogger();

    private final Configuration configuration;

    private final DependencyResolver dependencyResolver;

    MavenProjectConfigurator(Configuration configuration) {
        this.configuration = configuration;
        this.dependencyResolver = new DependencyResolver(configuration);
    }

    boolean configureTestRunner(Model model) {
        final List<Plugin> effectiveTestRunnerPluginConfigurations = getEffectivePlugins(model);

        if (!effectiveTestRunnerPluginConfigurations.isEmpty()) {
            logger.debug("Enabling Smart Testing %s for plugin %s in %s module", ExtensionVersion.version().toString(),
                effectiveTestRunnerPluginConfigurations.stream()
                    .map(Plugin::getArtifactId)
                    .collect(Collectors.toList()).toString(), model.getArtifactId());

            dependencyResolver.addRequiredDependencies(model);

            effectiveTestRunnerPluginConfigurations
                .forEach(plugin -> {
                    dependencyResolver.removeAndRegisterFirstCustomProvider(model, plugin);
                    dependencyResolver.addAsPluginDependency(plugin);
                });
            return true;
        } else {
            logger.debug("Disabling Smart Testing %s in %s module. Reason: No executable test plugin is set.",
                ExtensionVersion.version().toString(), model.getArtifactId());
            return false;
        }
    }

    private List<Plugin> getEffectivePlugins(Model model) {
        final List<Plugin> testRunnerPluginConfigurations = model.getBuild().getPlugins()
            .stream()
            .filter(
                plugin -> ApplicablePlugins.contains(plugin.getArtifactId()) && hasMinimumVersionRequired(plugin, model))
            .filter(this::hasPluginSelectionConfigured)
            .collect(Collectors.toList());

        if (areNotApplicableTestingPlugins(testRunnerPluginConfigurations) && isNotPomProject(model)) {
            failBecauseOfMissingApplicablePlugin(model);
        }

        return removePluginsThatAreSkipped(testRunnerPluginConfigurations, model);
    }

    private List<Plugin> removePluginsThatAreSkipped(List<Plugin> testRunnerPluginConfigurations, Model model) {
        SkipModuleChecker skipModuleChecker = new SkipModuleChecker(model);
        if (skipModuleChecker.areAllTestsSkipped()) {
            return Collections.emptyList();
        }
        return testRunnerPluginConfigurations.stream()
            .filter(testRunnerPlugin -> !(ApplicablePlugins.FAILSAFE.hasSameArtifactId(testRunnerPlugin.getArtifactId())
                && skipModuleChecker.areIntegrationTestsSkipped()))
            .filter(testRunnerPlugin -> !(ApplicablePlugins.SUREFIRE.hasSameArtifactId(testRunnerPlugin.getArtifactId())
                && skipModuleChecker.areUnitTestsSkipped()))
            .collect(Collectors.toList());
    }

    private boolean hasPluginSelectionConfigured(Plugin plugin) {
        if (configuration.isApplyToDefined()) {
            return plugin.getArtifactId().contains(configuration.getApplyTo());
        }
        // If not set the plugin is usable
        return true;
    }

    private boolean hasMinimumVersionRequired(Plugin plugin, Model model) {
        Version version = Version.from(plugin.getVersion().trim());
        if (!version.isGreaterOrEqualThan(MINIMUM_VERSION)) {
            failBecauseOfPluginVersionMismatch(model);
        }
        return true;
    }

    private void failBecauseOfMissingApplicablePlugin(Model model) {
        logCurrentPlugins(model);
        throw new IllegalStateException(
            String.format("Smart testing must be used with any of %s plugin(s). Please verify <plugins> section in your pom.xml",
                (configuration.isApplyToDefined()) ? configuration.getApplyTo() : ApplicablePlugins.ARTIFACT_IDS_LIST.toString()));
    }

    private void failBecauseOfPluginVersionMismatch(Model model) {
        logCurrentPlugins(model);
        throw new IllegalStateException(
            String.format("Smart testing must be used with any of %s plugins with minimum version %s. Please add or update one of the plugin in <plugins> section in your pom.xml",
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
