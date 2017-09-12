package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.arquillian.smart.testing.Logger;

import static org.arquillian.smart.testing.spi.TestResult.TEMP_REPORT_DIR;

class SurefireReportStorage {

    static final String SUREFIRE_REPORTS_DIR_NAME = "surefire-reports";
    private static final Logger logger = Logger.getLogger(SurefireReportStorage.class);

    static void copySurefireReports(Model model) {
        Build build = model.getBuild();
        if (build != null && build.getDirectory() != null) {
            File targetDir = new File(build.getDirectory());
            if (targetDir.exists() && targetDir.isDirectory()) {
                File[] surefireReports =
                    targetDir.listFiles(file -> file.isDirectory() && SUREFIRE_REPORTS_DIR_NAME.equals(file.getName()));
                if (surefireReports.length > 0) {
                    copyReportsDirectory(model, surefireReports[0]);
                }
            }
        }
    }

    private static void copyReportsDirectory(Model model, File surefireReportsDir) {
        File reportsDir = new File(model.getProjectDirectory(), TEMP_REPORT_DIR);
        logger.fine("Copying surefire report directory from [%s] to [%s]", surefireReportsDir, reportsDir);

        if (!reportsDir.exists()) {
            try {
                Files.createDirectory(reportsDir.toPath());
            } catch (IOException e) {
                logger.severe("There occurred an error when the directory %s was being created: %s", reportsDir,
                    e.getMessage());
                return;
            }
        }

        Arrays.stream(surefireReportsDir.listFiles())
            .filter(file -> file.isFile() && file.getName().endsWith(".xml"))
            .forEach(file -> copyReportFile(file, reportsDir));
    }

    private static void copyReportFile(File src, File destDir) {
        File destination = new File(destDir, src.getName());
        try {
            FileUtils.copyFile(src, destination);
        } catch (IOException e) {
            logger.severe("There occurred an error when the file %s was being copied to %s. See the error message: %s",
                src, destination, e.getMessage());
        }
    }

    static void purgeReports(MavenSession session) {
        session.getAllProjects().forEach(mavenProject -> {
            File reportsDir = new File(mavenProject.getModel().getProjectDirectory(), TEMP_REPORT_DIR);
            logger.fine("Deleting .reports directory at location %s", reportsDir);

            if (reportsDir.exists()) {
                try {
                    FileUtils.deleteDirectory(reportsDir);
                } catch (IOException e) {
                    logger.severe("There occurred an error when the directory %s was being removed: %s", reportsDir,
                        e.getMessage());
                }
            }
        });
    }
}
