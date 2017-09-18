package org.arquillian.smart.testing.ftest.customAssertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class ProjectReportAssert extends AbstractAssert<ProjectReportAssert, Project> {

    ProjectReportAssert(Project actual) {
        super(actual, ProjectReportAssert.class);
    }

    public ProjectReportAssert doesNotContainFile(String fileName) throws IOException {
        List<Path> reports = Files
            .walk(actual.getRoot())
            .filter(path -> path.toFile().getName().equals(fileName))
            .collect(Collectors.toList());
        Assertions.assertThat(reports).as("There should be no file %s present", fileName).isEmpty();
        return this;
    }
}
