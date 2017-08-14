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
import static org.arquillian.smart.testing.report.SmartTestExecutionReport.SMART_TESTING_REPORT_DIR;
import static org.arquillian.smart.testing.report.SmartTestExecutionReport.SMART_TESTING_REPORT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class SmartTestExecutionReportUsingPropertyTest {
    
    private static final String REPORT_TO_VERIFY_PATH = "src/test/resources/sample-report.xml";
    private static final String REPORT_PATH = "target/smart-testing-report/report.xml";

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    private SmartTestExecutionReport smartTestExecutionReport;

    @Before
    public void configureSmartTestingExecution() {
        System.setProperty(SMART_TESTING, "new,changed");
        final TestSelection testSelectionNew = new TestSelection(SmartTestExecutionReportTest.class.getName(), "new");
        final TestSelection testSelectionChanged = new TestSelection(SmartTestExecutionReportTest.class.getName(), "changed");
        final TestSelection anotherNewTestSelection = new TestSelection(SmartTestExecutionReportUsingPropertyTest.class.getName(), "new");
        smartTestExecutionReport = new SmartTestExecutionReport(asList(testSelectionNew, anotherNewTestSelection, testSelectionChanged), Configuration.load());
    }

    @Test
    public void should_generate_report_in_report_dir_with_given_name() {
        // given
        System.setProperty(SMART_TESTING_REPORT_DIR, "target/smart-testing-report/");
        System.setProperty(SMART_TESTING_REPORT_NAME, "report");

        // when
        smartTestExecutionReport.create();

        // then
        assertThat(new File(REPORT_PATH))
            .exists()
            .hasSameContentAs(new File(REPORT_TO_VERIFY_PATH));
    }

    @Test
    public void should_generate_report_in_report_dir_with_given_name_() {
        // given
        System.setProperty(SMART_TESTING_REPORT_DIR, "target/smart-testing-report");
        System.setProperty(SMART_TESTING_REPORT_NAME, "report");

        // when
        smartTestExecutionReport.create();

        // then
        assertThat(new File(REPORT_PATH)).exists();
    }

    @Test
    public void should_generate_report_with_given_report_dir_and_name_with_extension() {
        // given
        System.setProperty(SMART_TESTING_REPORT_DIR, "target/smart-testing-report/");
        System.setProperty(SMART_TESTING_REPORT_NAME, "report.xml");

        // when
        smartTestExecutionReport.create();

        // then
        assertThat(new File(REPORT_PATH)).exists();
    }

    @Test
    public void should_generate_report_in_default_dir() {
        // when
        smartTestExecutionReport.create();

        // then
        assertThat(new File("target/smart-testing-report.xml")).exists();
    }

    @Test
    public void should_generate_report_with_given_report_dir_and_name() {
        // given
        System.setProperty(SMART_TESTING_REPORT_DIR, "target/smart-report");
        System.setProperty(SMART_TESTING_REPORT_NAME, "reportName");

        // when
        smartTestExecutionReport.create();

        // then
        assertThat(new File("target/smart-report/reportName.xml")).exists();
    }

}
