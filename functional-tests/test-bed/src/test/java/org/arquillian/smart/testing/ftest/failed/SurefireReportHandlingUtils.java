package org.arquillian.smart.testing.ftest.failed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.project.Project;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.arquillian.smart.testing.ftest.testbed.project.ProjectBuilder.IN_PROJECT_DIR;
import static org.arquillian.smart.testing.ftest.testbed.project.ProjectBuilder.TEST_REPORT_PREFIX;

public class SurefireReportHandlingUtils {

    private static final String AFFECTED_MODULE_PATH = "container/impl-base";

    public static void copySurefireReportsFromPreviousBuild(Project project, List<Path> reportPaths) throws IOException {
        Path path = Paths.get(project.getRoot().resolve(AFFECTED_MODULE_PATH) + File.separator + IN_PROJECT_DIR);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            for (Path reportSrc : reportPaths) {
                Path reportDest = Files.createFile(path.resolve(reportSrc.getFileName()));
                Files.copy(reportSrc, reportDest, REPLACE_EXISTING);
            }
        }
    }

    public static List<Path> storeSurefireReportsForFailingBuild(Project project) throws IOException {
        return Files.walk(Paths.get(project.getRoot().resolve(AFFECTED_MODULE_PATH) + "/target/surefire-reports"))
            .filter(path -> path.getFileName().toString().startsWith(TEST_REPORT_PREFIX))
            .collect(Collectors.toList());
    }
}
