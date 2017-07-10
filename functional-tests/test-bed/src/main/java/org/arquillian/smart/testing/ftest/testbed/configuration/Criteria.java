package org.arquillian.smart.testing.ftest.testbed.configuration;

import org.apache.maven.model.Dependency;

import static org.arquillian.smart.testing.ftest.testbed.project.Project.SMART_TESTING_VERSION;

public enum Criteria {
    AFFECTED("affected"),
    NEW("changed");

    private final String dependencySuffix;

    Criteria(String dependencySuffix) {
        this.dependencySuffix = dependencySuffix;
    }

    public Dependency getMavenDependency() {
        Dependency mavenDependency = new Dependency();
        mavenDependency.setGroupId("org.arquillian.smart.testing");
        mavenDependency.setVersion(SMART_TESTING_VERSION);
        mavenDependency.setArtifactId("smart-testing-strategy-" + dependencySuffix);
        return mavenDependency;
    }
}
