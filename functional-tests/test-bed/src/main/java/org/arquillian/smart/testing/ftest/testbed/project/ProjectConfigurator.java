package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.configuration.Mode;
import org.arquillian.smart.testing.ftest.testbed.configuration.Strategy;

public class ProjectConfigurator {

    private Strategy[] strategies;
    private Mode mode;

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

    public Project enable() {
        final Path rootPom = Paths.get(root.toString(), File.separator, "pom.xml");
        final MavenExtensionRegisterer mavenExtensionRegisterer = new MavenExtensionRegisterer(rootPom, this);
        mavenExtensionRegisterer.addSmartTestingExtension();
        final String strategies = Arrays.stream(getStrategies()).map(Strategy::getName).collect(Collectors.joining(","));
        this.project.buildOptions().withSystemProperties("smart.testing", strategies, "smart.testing.mode", getMode().getName()).configure();
        return this.project;
    }

}
