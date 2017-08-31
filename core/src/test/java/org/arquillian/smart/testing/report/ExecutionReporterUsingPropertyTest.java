package org.arquillian.smart.testing.report;

import java.io.File;
import java.util.Properties;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.TestSelection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;

import static java.util.Arrays.asList;
import static org.arquillian.smart.testing.Configuration.SMART_TESTING;
import static org.arquillian.smart.testing.Configuration.SMART_TESTING_MODE;
import static org.arquillian.smart.testing.report.ExecutionReporterHelper.getBaseDir;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.SMART_TESTING_REPORT_DIR;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.SMART_TESTING_REPORT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ExecutionReporterUsingPropertyTest {
    
    private static final String REPORT_TO_VERIFY_PATH = "src/test/resources/sample-report.xml";
    private static final String REPORT_PATH = "target/smart-testing-report/report.xml";

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ClearSystemProperties clearSystemProperties = new ClearSystemProperties(getSmartTestingProperties());

    private SmartTestingReportGenerator smartTestingReportGenerator;

    @Test
    public void should_generate_report_in_report_dir_with_given_name() {
        // given
        System.setProperty(SMART_TESTING_REPORT_DIR, "target/smart-testing-report");
        System.setProperty(SMART_TESTING_REPORT_NAME, "report");
        System.setProperty(SMART_TESTING, "new,changed");
        System.setProperty(SMART_TESTING_MODE, "selecting");
        final TestSelection newChangedTestSelection = new TestSelection(ExecutionReporterTest.class.getName(), "new", "changed");
        final TestSelection newTestSelection = new TestSelection(ExecutionReporterUsingPropertyTest.class.getName(), "new");
        smartTestingReportGenerator = new SmartTestingReportGenerator(asList(newChangedTestSelection, newTestSelection), Configuration.load(), getBaseDir(getClass()));

        // when
        smartTestingReportGenerator.generateReport();

        // then
        assertThat(new File(REPORT_PATH))
            .exists()
            .hasSameContentAs(new File(REPORT_TO_VERIFY_PATH));
    }

    private static String[] getSmartTestingProperties() {
        final Properties properties = System.getProperties();

        return properties.stringPropertyNames()
            .stream()
            .filter(name -> name.startsWith("smart.testing"))
            .toArray(String[]::new);
    }
 }
