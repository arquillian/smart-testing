package org.arquillian.smart.testing.report;

import java.io.File;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.TestSelection;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ExecutionReporterTest {

    @Test
    public void should_generate_report_in_default_dir() {
        // given
        final TestSelection testSelectionNew = new TestSelection(ExecutionReporterTest.class.getName(), "new");
        final TestSelection testSelectionChanged = new TestSelection(ExecutionReporterTest.class.getName(), "changed");
        final ExecutionReporter executionReporter =
            new ExecutionReporter(asList(testSelectionNew, testSelectionChanged), Configuration.load());

        // when
        executionReporter.createReport();

        // then
        assertThat(new File("target/smart-testing-report.xml")).exists();
    }
}
