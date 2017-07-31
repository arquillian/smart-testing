package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.configuration.Mode;
import org.arquillian.smart.testing.ftest.testbed.configuration.Strategy;

public class ProjectConfigurator {

    private static final String SMART_TESTING = "smart.testing";
    private static final String SMART_TESTING_MODE = "smart.testing.mode";
    private static final String SMART_TESTING_VERSION = "smart.testing.version";

    private Strategy[] strategies;
    private Mode mode;
    private String version;

    private final Project project;
    private final Path root;

    ProjectConfigurator(Project project, Path root) {
        this.project = project;
        this.root = root;
    }

    Strategy[] getStrategies() {
        return strategies;
    }

    Mode getMode() {
        return mode;
    }

    public ProjectConfigurator executionOrder(Strategy... strategies) {
        this.strategies = strategies;
        return this;
    }

    public ProjectConfigurator inMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public ProjectConfigurator version(String version) {
        this.version = version;
        return this;
    }

    public Project enable() {

        final Path rootPom = Paths.get(root.toString(), "pom.xml");
        final MavenExtensionRegisterer mavenExtensionRegisterer = new MavenExtensionRegisterer(rootPom);
        String currentVersion = resolveVersion();
        mavenExtensionRegisterer.addSmartTestingExtension(currentVersion);
        final String strategies = Arrays.stream(getStrategies()).map(Strategy::getName).collect(Collectors.joining(","));
        this.project.buildOptions()
            .withSystemProperties(SMART_TESTING, strategies, SMART_TESTING_MODE, getMode().getName(),
                SMART_TESTING_VERSION, currentVersion)
            .configure();
        return this.project;
    }

    private String resolveVersion() {

        if (this.version == null) {
            String systemProperty = System.getProperty(SMART_TESTING_VERSION);

            if (systemProperty == null) {
                return readVersionFromExtensionFile();
            }

            return systemProperty;
        }

        return this.version;
    }

    private String readVersionFromExtensionFile() {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(ProjectConfigurator.class.getResourceAsStream("/extension_version")))) {
            return reader.readLine().trim();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read extension version", e);
        }
    }
}
