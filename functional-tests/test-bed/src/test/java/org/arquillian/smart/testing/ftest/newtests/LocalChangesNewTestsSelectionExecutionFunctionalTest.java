package org.arquillian.smart.testing.ftest.newtests;

import java.util.List;
import org.arquillian.smart.testing.ftest.TestBedTemplate;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Criteria.NEW;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalChangesNewTestsSelectionExecutionFunctionalTest extends TestBedTemplate {

    @Test
    public void should_only_execute_new_tests_related_to_single_local_change() throws Exception {
        // given
        project.configureSmartTesting()
                    .executionOrder(NEW)
                    .inMode(SELECTING)
               .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Adds new unit test");

        // when
        final List<TestResult> actualTestResults = project.build();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_only_execute_new_tests_when_multiple_local_changes_applied() throws Exception {
        // given
        project.configureSmartTesting()
                    .executionOrder(NEW)
                    .inMode(SELECTING)
            .enable();

        // we ignore expected tests results for this commits, as they are changes to existing ones,
        // but still want to apply the changes
        project.applyAsLocalChanges("Single method body modification - sysout",
            "Inlined variable in a method");

        final List<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Adds new unit test");

        // when
        final List<TestResult> actualTestResults = project.build();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_not_execute_single_tests_as_pattern_excluded_from_configuration() throws Exception {
        // given
        project.configureSmartTesting()
            .executionOrder(NEW)
            .inMode(SELECTING)
            .withExcludes("**/*TestCase.java")
            .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Adds new unit test");

        // when
        final List<TestResult> actualTestResults = project.build();

        // then
        assertThat(actualTestResults).hasSize(0);
    }

}
