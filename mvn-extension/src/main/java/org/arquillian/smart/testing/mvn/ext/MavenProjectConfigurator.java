package org.arquillian.smart.testing.mvn.ext;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.mvn.ext.dependencies.DependencyResolver;
import org.arquillian.smart.testing.mvn.ext.dependencies.ExtensionVersion;
import org.arquillian.smart.testing.mvn.ext.dependencies.Version;
import org.codehaus.plexus.util.xml.Xpp3Dom;

class MavenProjectConfigurator {

    private static final Version MINIMUM_VERSION = Version.from("2.19.1");

    private static Logger logger = Logger.getLogger(MavenProjectConfigurator.class);

    private final Configuration configuration;

    private final DependencyResolver dependencyResolver;

    MavenProjectConfigurator(Configuration configuration) {
        this.configuration = configuration;
        this.dependencyResolver = new DependencyResolver(configuration);
    }

    void showPom(Model model) {
        try (StringWriter pomOut = new StringWriter()) {
            new MavenXpp3Writer().write(pomOut, model);
        } catch (IOException e) {
            throw new RuntimeException("Failed writing updated pom file: " + model.getPomFile().getAbsolutePath(), e);
        }
    }

    void configureTestRunner(Model model) {
        final List<Plugin> effectiveTestRunnerPluginConfigurations = getEffectivePlugins(model);

        if (!effectiveTestRunnerPluginConfigurations.isEmpty()) {
            logger.info("Enabling Smart Testing %s for %s", ExtensionVersion.version().toString(),
                effectiveTestRunnerPluginConfigurations.stream()
                    .map(Plugin::getArtifactId)
                    .collect(Collectors.toList()).toString());

            dependencyResolver.addRequiredDependencies(model);

            effectiveTestRunnerPluginConfigurations
                .forEach(testRunnerPlugin -> {
                    dependencyResolver.addAsPluginDependency(testRunnerPlugin);
                    final Object configuration = testRunnerPlugin.getConfiguration();
                    if (configuration != null) {
                        final Xpp3Dom configurationDom = (Xpp3Dom) configuration;
                        final Xpp3Dom properties = getOrCreatePropertiesChild(configurationDom);
                        properties.addChild(defineUsageMode());
                        properties.addChild(defineTestSelectionCriteria());
                    }
                });
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

    boolean isSkipTestExecutionSet() {
        return isSkipTests() || isSkip();
    }

    private boolean isSkipTests() {
        return Boolean.valueOf(System.getProperty("skipTests", "false"));
    }

    private boolean isSkipITs() {
        return Boolean.valueOf(System.getProperty("skipITs", "false"));
    }

    private boolean isSkip() {
        return Boolean.valueOf(System.getProperty("maven.test.skip", "false"));
    }

    private Xpp3Dom defineTestSelectionCriteria() {
        final Xpp3Dom strategies = new Xpp3Dom("strategies");
        final StringJoiner stringJoiner = new StringJoiner(",");
        for (final String strategy : configuration.getStrategies()) {
            stringJoiner.add(strategy);
        }
        strategies.setValue(stringJoiner.toString());
        return strategies;
    }

    private Xpp3Dom defineUsageMode() {
        final Xpp3Dom usage = new Xpp3Dom("usage");
        usage.setValue(configuration.getMode().getName());
        return usage;
    }

    private Xpp3Dom getOrCreatePropertiesChild(Xpp3Dom configurationDom) {
        Xpp3Dom properties = configurationDom.getChild("properties");
        if (properties == null) {
            properties = new Xpp3Dom("properties");
            configurationDom.addChild(properties);
        }
        return properties;
    }

    private void failBecauseOfPluginVersionMismatch(Model model) {
        logger.severe(
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
            .forEach(plugin -> logger.severe("Current applicable plugin: %s:%s:%s", plugin.getGroupId(),
                plugin.getArtifactId(), plugin.getVersion()));
    }
}
