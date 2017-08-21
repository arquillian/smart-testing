package org.arquillian.smart.testing.report;

import java.io.File;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.TestSelection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;

import static java.util.Arrays.asList;
import static org.arquillian.smart.testing.Configuration.SMART_TESTING;
import static org.arquillian.smart.testing.report.ExecutionReporter.SMART_TESTING_REPORT_DIR;
import static org.arquillian.smart.testing.report.ExecutionReporter.SMART_TESTING_REPORT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ExecutionReporterUsingPropertyTest {
    
    private static final String REPORT_TO_VERIFY_PATH = "src/test/resources/sample-report.xml";
    private static final String REPORT_PATH = "target/smart-testing-report/report.xml";

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    private ExecutionReporter executionReporter;

    @Before
    public void configureSmartTestingExecution() {
        System.setProperty(SMART_TESTING, "new,changed");
        final TestSelection newChangedTestSelection = new TestSelection(ExecutionReporterTest.class.getName(), "new", "changed");
        final TestSelection newTestSelection = new TestSelection(ExecutionReporterUsingPropertyTest.class.getName(), "new");
        executionReporter = new ExecutionReporter(asList(newChangedTestSelection, newTestSelection), Configuration.load());
    }

    @Test
    public void should_generate_report_in_report_dir_with_given_name() {
        // given
        System.setProperty(SMART_TESTING_REPORT_DIR, "target/smart-testing-report/");
        System.setProperty(SMART_TESTING_REPORT_NAME, "report");

        // when
        executionReporter.createReport();

        // then
        assertThat(new File(REPORT_PATH))
            .exists()
            .hasSameContentAs(new File(REPORT_TO_VERIFY_PATH));
    }
}
