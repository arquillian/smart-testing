package org.arquillian.smart.testing.report;

import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.TestSelection;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static java.util.Arrays.asList;

public class SmartTestExecutionReportTest {

    @Test
    public void should_return_merged_test_selections_if_test_selection_has_same_class() {
        // given
        final TestSelection testSelectionNew = new TestSelection(SmartTestExecutionReportTest.class.getName(), "new");
        final TestSelection testSelectionChanged = new TestSelection(SmartTestExecutionReportTest.class.getName(), "changed");

        // when
        SmartTestExecutionReport smartTestExecutionReport =
            new SmartTestExecutionReport(asList(testSelectionNew, testSelectionChanged), Configuration.load());

        //then
        Assertions.assertThat(smartTestExecutionReport.getTestSelections())
            .hasSize(1)
            .flatExtracting("types").containsExactly("new", "changed");
    }

    @Test
    public void should_not_return_merged_test_selections_if_test_selection_has_same_class() {
        // given
        final TestSelection testSelectionNew = new TestSelection(SmartTestExecutionReportTest.class.getName(), "new");
        final TestSelection testSelectionChanged =
            new TestSelection(SmartTestExecutionReportUsingPropertyTest.class.getName(), "changed");

        // when
        SmartTestExecutionReport smartTestExecutionReport =
            new SmartTestExecutionReport(asList(testSelectionNew, testSelectionChanged), Configuration.load());

        // then
        Assertions.assertThat(smartTestExecutionReport.getTestSelections())
            .hasSize(2)
            .flatExtracting("types").containsExactly("new", "changed");
    }
}
