package org.arquillian.smart.testing.ftest.mixed;

import java.util.List;
import org.arquillian.smart.testing.ftest.TestBedTemplate;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalChangesMixedStrategySelectionExecutionFunctionalTest extends TestBedTemplate {

    @Test
    public void should_execute_all_new_tests_and_related_to_production_code_changes() throws Exception {
        // given
        project.configureSmartTesting()
                .executionOrder(NEW, AFFECTED)
                .inMode(SELECTING)
            .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Single method body modification - sysout",
                "Inlined variable in a method", "Adds new unit test");

        // when
        final List<TestResult> actualTestResults = project.build();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }
}
