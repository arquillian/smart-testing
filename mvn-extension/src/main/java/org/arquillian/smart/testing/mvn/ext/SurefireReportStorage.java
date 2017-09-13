package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.hub.storage.local.SubDirectoryDirectoryAction;

import static org.arquillian.smart.testing.spi.TestResult.TEMP_REPORT_DIR;

class SurefireReportStorage {

    static final String SUREFIRE_REPORTS_DIR_NAME = "surefire-reports";
    private static final Logger logger = Logger.getLogger();

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
        SubDirectoryDirectoryAction reportsDirectory =
            new LocalStorage(model.getProjectDirectory())
                .execution()
                .directory(TEMP_REPORT_DIR);
        logger.debug("Copying surefire report directory from [%s] to [%s]", surefireReportsDir,
            reportsDirectory.getPath());

        reportsDirectory.create(surefireReportsDir.toPath(), file -> file.isFile() && file.getName().endsWith(".xml"),
            true);
    }
}
