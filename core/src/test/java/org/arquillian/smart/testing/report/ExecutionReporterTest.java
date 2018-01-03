package org.arquillian.smart.testing.report;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.arquillian.smart.testing.Constants.CURRENT_DIR;
import static org.arquillian.smart.testing.hub.storage.local.AfterExecutionLocalStorage.REPORTING_SUBDIRECTORY;
import static org.arquillian.smart.testing.hub.storage.local.AfterExecutionLocalStorage.SMART_TESTING_TARGET_DIRECTORY_NAME;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class ExecutionReporterTest {

    @Test
    public void should_generate_report_in_default_dir() {
        // given
        final TestSelection testSelectionNew = new TestSelection(ExecutionReporterTest.class.getName(), "new");
        final TestSelection testSelectionChanged = new TestSelection(ExecutionReporterTest.class.getName(), "changed");
        final SmartTestingReportGenerator smartTestingReportGenerator =
            new SmartTestingReportGenerator(asList(testSelectionNew, testSelectionChanged), ConfigurationLoader.load(
                CURRENT_DIR),
                System.getProperty("user.dir"));

        // when
        smartTestingReportGenerator.generateReport();

        // then
        Path report = Paths.get("target", SMART_TESTING_TARGET_DIRECTORY_NAME, REPORTING_SUBDIRECTORY, REPORT_FILE_NAME);
        assertThat(report).exists();
    }
}
