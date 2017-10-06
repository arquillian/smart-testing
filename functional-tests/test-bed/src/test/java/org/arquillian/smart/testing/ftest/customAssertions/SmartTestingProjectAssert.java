package org.arquillian.smart.testing.ftest.customAssertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class SmartTestingProjectAssert extends AbstractAssert<SmartTestingProjectAssert, Project> {

    SmartTestingProjectAssert(Project actual) {
        super(actual, SmartTestingProjectAssert.class);
    }

    public static SmartTestingProjectAssert assertThat(Project project) {
        return new SmartTestingProjectAssert(project);
    }

    public SmartTestingProjectAssert doesNotContainDirectory(String dirName) throws IOException {
        List<Path> reports = Files
            .walk(actual.getRoot())
            .filter(path -> path.toFile().getName().equals(dirName))
            .collect(Collectors.toList());
        Assertions.assertThat(reports).as("There should be no directory %s present", dirName).isEmpty();
        return this;
    }
}
