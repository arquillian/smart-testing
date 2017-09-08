package org.arquillian.smart.testing.ftest.failed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.assertj.core.api.Assertions;

import static org.arquillian.smart.testing.spi.TestResult.TEMP_REPORT_DIR;

public class ReportsVerifier {

    static void assertNoReportsDirectoryPresent(Project project) throws IOException {
        List<Path> reports = Files
            .walk(project.getRoot())
            .filter(path -> path.toFile().getName().equals(TEMP_REPORT_DIR))
            .collect(Collectors.toList());
        Assertions.assertThat(reports).as("There should be no .reports directory present").isEmpty();
    }
}
