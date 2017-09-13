package org.arquillian.smart.testing.ftest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.assertj.core.api.Assertions;

public class FileVerifier {

    public static void assertThatFileIsNotPresent(Project project, String fileName) throws IOException {
        List<Path> reports = Files
            .walk(project.getRoot())
            .filter(path -> path.toFile().getName().equals(fileName))
            .collect(Collectors.toList());
        Assertions.assertThat(reports).as("There should be no file %s present", fileName).isEmpty();
    }
}
