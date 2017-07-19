package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.arquillian.smart.testing.ftest.testbed.configuration.Criteria;
import org.arquillian.smart.testing.ftest.testbed.configuration.Mode;

public class ProjectConfigurator {

    private Criteria[] criteria;
    private Mode mode;
    private String[] includes;
    private String[] excludes;

    private final Project project;
    private final Path root;

    ProjectConfigurator(Project project, Path root) {
        this.project = project;
        this.root = root;
    }

    Criteria[] getCriteria() {
        return criteria;
    }

    Mode getMode() {
        return mode;
    }

    public ProjectConfigurator executionOrder(Criteria ... criteria) {
        this.criteria = criteria;
        return this;
    }

    public ProjectConfigurator inMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public ProjectConfigurator withIncludes(String... includes) {
        this.includes = includes;
        return this;
    }

    public ProjectConfigurator withExcludes(String... excludes) {
        this.excludes = excludes;
        return this;
    }


    public Project enable() {
        final Path rootPom = Paths.get(root.toString(), File.separator, "pom.xml");
        final MavenConfigurator mavenConfigurator = new MavenConfigurator(rootPom, this);
        mavenConfigurator.addRequiredDependencies();
        mavenConfigurator.configureTestRunner();
        mavenConfigurator.update();
        return this.project;
    }

    public String[] getIncludes() {
        return includes;
    }

    public String[] getExcludes() {
        return excludes;
    }
}
