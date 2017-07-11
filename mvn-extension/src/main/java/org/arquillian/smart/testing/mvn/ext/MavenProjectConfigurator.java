package org.arquillian.smart.testing.mvn.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.StringJoiner;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;

class MavenProjectConfigurator {

    private static final List<String> APPLICABLE_PLUGINS =  Arrays.asList("maven-surefire-plugin", "maven-failsafe-plugin");

    private final Configuration configuration;

    MavenProjectConfigurator(Configuration configuration) {
        this.configuration = configuration;
    }

    /*
    - based on selected strategy e.g. "new" we have to know what is the actual dependency we have to add
    - ? naming convention? -> propert

     */

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
        final Optional<Plugin> testRunnerPluginConfiguration = model.getBuild().getPlugins()
            .stream()
            .filter(plugin -> APPLICABLE_PLUGINS.contains(plugin.getArtifactId()))
            .findFirst();

        if (testRunnerPluginConfiguration.isPresent()) {
            final Plugin testRunnerPlugin = testRunnerPluginConfiguration.get();
            testRunnerPlugin.addDependency(smartTestingProviderDependency());
            final Object configuration = testRunnerPlugin.getConfiguration();
            if (configuration != null) {
                final Xpp3Dom configurationDom = (Xpp3Dom) configuration;
                final Xpp3Dom properties = getOrCreatePropertiesChild(configurationDom);
                properties.addChild(defineUsageMode());
                properties.addChild(defineTestSelectionCriteria());
            }
        }
        // TODO what if there is no plugin defined?
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
        usage.setValue(configuration.getMode());
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
        smartTestingSurefireProvider.setVersion("0.0.1-SNAPSHOT"); // TODO make it auto-discoverable
        smartTestingSurefireProvider.setScope("runtime");
        return smartTestingSurefireProvider;
    }
}
