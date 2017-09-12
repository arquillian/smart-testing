package org.arquillian.smart.testing.ftest.failed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.arquillian.smart.testing.ftest.testbed.project.ProjectBuilder.TEST_REPORT_PREFIX;
import static org.arquillian.smart.testing.ftest.testbed.testresults.SurefireReportReader.loadTestResults;
import static org.arquillian.smart.testing.spi.TestResult.TEMP_REPORT_DIR;

class TestReportHandler {

    static void copySurefireReports(Project project) throws IOException {
        final List<Path> reportPaths = findSurefireReportsForFailingTests(project);
        for (Path reportSrc : reportPaths) {
            String targetDir = reportSrc.toString().split("target")[0];
            Path path = Paths.get(project.getRoot().resolve(targetDir) + File.separator + TEMP_REPORT_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            Path reportDest = Files.createFile(path.resolve(reportSrc.getFileName()));
            Files.copy(reportSrc, reportDest, REPLACE_EXISTING);
        }
    }

    private static List<Path> findSurefireReportsForFailingTests(Project project) throws IOException {
        return Files.walk(project.getRoot())
            .filter(path -> path.getFileName().toString().startsWith(TEST_REPORT_PREFIX) && isAnyTestFailing(path))
            .collect(Collectors.toList());
    }

    private static boolean isAnyTestFailing(Path path) {
        try {
            final Collection<TestResult> testResults = loadTestResults(new FileInputStream(path.toFile()));
            return testResults.stream()
                .anyMatch(TestResult::isFailing);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }
}
