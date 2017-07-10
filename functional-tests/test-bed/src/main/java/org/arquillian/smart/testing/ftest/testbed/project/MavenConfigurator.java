package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.arquillian.smart.testing.ftest.testbed.configuration.Criteria;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static java.util.Arrays.asList;

class MavenConfigurator {

    private static final List<String> APPLICABLE_PLUGINS =  asList("maven-surefire-plugin", "maven-failsafe-plugin");

    private final Model model;
    private final ProjectConfigurator projectConfigurator;

    MavenConfigurator(Path rootPom, ProjectConfigurator projectConfigurator) {
        this.model = readPom(rootPom);
        this.projectConfigurator = projectConfigurator;
    }

    void addRequiredDependencies() {
        model.addDependency(smartTestingProviderDependency());
        final Criteria[] criteria = projectConfigurator.getCriteria();
        for (final Criteria criterion : criteria) {
            model.addDependency(criterion.getMavenDependency());
        }
    }

    void configureTestRunner() {
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
    }

    void update() {
        try (BufferedWriter pomOut = new BufferedWriter(new FileWriter(model.getPomFile()))) {
            new MavenXpp3Writer().write(pomOut, model);
        } catch (IOException e) {
            throw new RuntimeException("Failed writing updated pom file: " + model.getPomFile().getAbsolutePath(), e);
        }
    }

    private Model readPom(Path pom) {
        final Model model;
        final File pomFile = pom.toFile();
        try (BufferedReader pomIn = new BufferedReader(new FileReader(pomFile))) {
            final MavenXpp3Reader mvnReader = new MavenXpp3Reader();
            model = mvnReader.read(pomIn);
            model.setPomFile(pomFile);
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException("Failed while loading Maven POM from: " + pom.toAbsolutePath(), e);
        }
        return model;
    }

    private Xpp3Dom defineTestSelectionCriteria() {
        final Xpp3Dom strategies = new Xpp3Dom("strategies");
        final Criteria[] criteria = projectConfigurator.getCriteria();
        final StringJoiner stringJoiner = new StringJoiner(",");
        for (final Criteria criterion : criteria) {
            stringJoiner.add(criterion.name().toLowerCase());
        }
        strategies.setValue(stringJoiner.toString());
        return strategies;
    }

    private Xpp3Dom defineUsageMode() {
        final Xpp3Dom usage = new Xpp3Dom("usage");
        usage.setValue(projectConfigurator.getMode().name().toLowerCase());
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
        smartTestingSurefireProvider.setVersion(Project.SMART_TESTING_VERSION);
        return smartTestingSurefireProvider;
    }
}
