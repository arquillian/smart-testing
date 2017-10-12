package org.arquillian.smart.testing.ftest.customAssertions;

import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectAssert extends AbstractAssert<ProjectAssert, Project> {

    ProjectAssert(Project actual) {
        super(actual, ProjectAssert.class);
    }

    public static ProjectAssert assertThat(Project project) {
        return new ProjectAssert(project);
    }

    public ProjectAssert doesNotContainDirectory(String dirName) throws IOException {
        List<Path> reports = Files
            .walk(actual.getRoot())
            .filter(path -> path.toFile().getName().equals(dirName))
            .collect(Collectors.toList());
        Assertions.assertThat(reports).as("There should be no directory %s present", dirName).isEmpty();
        return this;
    }
}
