package org.arquillian.smart.testing.ftest.customAssertions;

import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;

public class CustomAssertions {

    public static ProjectReportAssert assertThat(Project project) {
        return new ProjectReportAssert(project);
    }

    public static BuildProjectAssert assertThat(BuiltProject module) {
        return new BuildProjectAssert(module);
    }
}
