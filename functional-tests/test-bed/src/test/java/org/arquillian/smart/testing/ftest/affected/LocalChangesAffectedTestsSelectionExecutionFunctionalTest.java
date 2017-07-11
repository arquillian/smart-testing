package org.arquillian.smart.testing.ftest.affected;

import java.util.List;
import org.arquillian.smart.testing.ftest.TestBedTemplate;
import org.arquillian.smart.testing.ftest.testbed.testresults.Status;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.assertj.core.api.Condition;
import org.assertj.core.groups.Tuple;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Criteria.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class LocalChangesAffectedTestsSelectionExecutionFunctionalTest extends TestBedTemplate {

    @Test
    public void should_only_execute_tests_related_to_single_local_change_in_production_code_when_affected_is_enabled()
        throws Exception {
        // given
        project.configureSmartTesting()
            .executionOrder(AFFECTED)
            .inMode(SELECTING)
            .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Single method body modification - sysout");

        // when
        final List<TestResult> actualTestResults = project.build();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_only_execute_tests_related_to_multiple_local_changes_in_production_code_when_affected_is_enabled()
        throws Exception {
        // given
        project.configureSmartTesting()
            .executionOrder(AFFECTED)
            .inMode(SELECTING)
            .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        final List<TestResult> actualTestResults = project.build();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_not_execute_any_tests_as_pattern_excluded_in_configuration()
        throws Exception {
        // given
        project.configureSmartTesting()
            .executionOrder(AFFECTED)
            .inMode(SELECTING)
            .withExcludes("**/*TestCase.java")
            .enable();

        project.applyAsLocalChanges("Single method body modification - sysout");

        // when
        final List<TestResult> actualTestResults = project.build();

        // then
        assertThat(actualTestResults).hasSize(0);
    }

    @Test
    public void should_execute_included_tests_in_configuration() throws Exception {
        // given
        project.configureSmartTesting()
            .executionOrder(AFFECTED)
            .inMode(SELECTING)
            .withExcludes("**/SyspropReplacementInArqXmlTestCase.java")
            .withIncludes("**/*TestCase.java")
            .enable();

        project.applyAsLocalChanges("Single method body modification - sysout");

        // when
        final List<TestResult> actualTestResults = project.build();

        // then
        assertThat(actualTestResults).hasSize(1)
            .extracting("className", "result")
            .contains(tuple("org.jboss.arquillian.config.impl.extension.ConfigurationRegistrarTestCase", Status.PASSED));

    }
}
