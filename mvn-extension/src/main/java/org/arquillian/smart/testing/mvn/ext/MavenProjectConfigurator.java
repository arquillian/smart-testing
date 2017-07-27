package org.arquillian.smart.testing.mvn.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

class MavenProjectConfigurator {

    private static final Version MINIMUM_VERSION = Version.from("2.19.1");
    private static final List<String> APPLICABLE_PLUGINS =
        Arrays.asList("maven-surefire-plugin", "maven-failsafe-plugin");

    private static Logger logger = Logger.getLogger(MavenProjectConfigurator.class);

    private final Configuration configuration;

    MavenProjectConfigurator(Configuration configuration) {
        this.configuration = configuration;
    }

    void addRequiredDependencies(Model model) {
        model.addDependency(smartTestingProviderDependency());
        final String[] strategies = configuration.getStrategies();
        try (InputStream strategyMapping = getClass().getClassLoader().getResourceAsStream("strategies.properties")) {
            if (strategyMapping == null) {
                throw new RuntimeException("Unable to load strategy definitions");
            }
            final Properties properties = new Properties();
            properties.load(strategyMapping);
            final StrategyDependencyResolver strategyDependencyResolver =
                new StrategyDependencyResolver(properties);
            final Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();
            for (final String strategy : strategies) {
                final Dependency dependency = dependencies.get(strategy);
                model.addDependency(dependency);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load strategy definitions", e);
        }
    }

    void showPom(Model model) {
        try (StringWriter pomOut = new StringWriter()) {
            new MavenXpp3Writer().write(pomOut, model);
        } catch (IOException e) {
            throw new RuntimeException("Failed writing updated pom file: " + model.getPomFile().getAbsolutePath(), e);
        }
    }

    void configureTestRunner(Model model) {
        final List<Plugin> testRunnerPluginConfigurations = model.getBuild().getPlugins()
            .stream()
            .filter(plugin -> APPLICABLE_PLUGINS.contains(plugin.getArtifactId()))
            .filter(plugin -> Version.from(plugin.getVersion().trim()).isGreaterOrEqualThan(MINIMUM_VERSION))
            .filter(plugin -> {
                if (configuration.isSmartTestingPluginDefined()) {
                    return configuration.getSmartTestingPlugin().equals(plugin.getArtifactId());
                }
                // If not set the plugin is usable
                return true;
            })
            .collect(Collectors.toList());

        if (areNotApplicableTestingPlugins(testRunnerPluginConfigurations) && isNotPomProject(model)) {

            logger.severe(
                "Smart testing must be used with any of %s plugins with minimum version %s. Please add or update one of the plugin in <plugins> section in your pom.xml",
                APPLICABLE_PLUGINS, MINIMUM_VERSION);
            logCurrentPlugins(model);
            throw new IllegalStateException(
                String.format("Smart testing must be used with any of %s plugins with minimum version %s",
                    APPLICABLE_PLUGINS, MINIMUM_VERSION));
        }

        for (Plugin testRunnerPlugin : testRunnerPluginConfigurations) {
            testRunnerPlugin.addDependency(smartTestingProviderDependency());
            final Object configuration = testRunnerPlugin.getConfiguration();
            if (configuration != null) {
                final Xpp3Dom configurationDom = (Xpp3Dom) configuration;
                final Xpp3Dom properties = getOrCreatePropertiesChild(configurationDom);
                properties.addChild(defineUsageMode());
                properties.addChild(defineTestSelectionCriteria());
            }
        }
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
            .filter(plugin -> APPLICABLE_PLUGINS.contains(plugin.getArtifactId()))
            .forEach(plugin -> logger.severe("Current applicable plugin: %s:%s:%s", plugin.getGroupId(),
                plugin.getArtifactId(), plugin.getVersion()));
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

    private Dependency smartTestingProviderDependency() {
        final Dependency smartTestingSurefireProvider = new Dependency();
        smartTestingSurefireProvider.setGroupId("org.arquillian.smart.testing");
        smartTestingSurefireProvider.setArtifactId("smart-testing-surefire-provider");
        smartTestingSurefireProvider.setVersion(ExtensionVersion.version().toString());
        smartTestingSurefireProvider.setScope("runtime");
        return smartTestingSurefireProvider;
    }
}
