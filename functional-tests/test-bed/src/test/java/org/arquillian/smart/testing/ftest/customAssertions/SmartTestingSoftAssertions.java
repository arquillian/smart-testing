package org.arquillian.smart.testing.ftest.customAssertions;

import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.assertj.core.api.SoftAssertions;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SmartTestingSoftAssertions extends SoftAssertions implements TestRule {

    public BuiltProjectAssert assertThat(BuiltProject actual) {
        return proxy(BuiltProjectAssert.class, BuiltProject.class, actual);
    }

    public ProjectAssert assertThat(Project actual) {
        return proxy(ProjectAssert.class, Project.class, actual);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            public void evaluate() throws Throwable {
                base.evaluate();
                assertAll();
            }
        };
    }
}
