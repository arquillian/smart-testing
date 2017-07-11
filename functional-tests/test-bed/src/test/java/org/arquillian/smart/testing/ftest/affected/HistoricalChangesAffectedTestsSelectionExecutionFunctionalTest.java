package org.arquillian.smart.testing.ftest.affected;

import java.util.List;
import org.arquillian.smart.testing.ftest.TestBedTemplate;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.assertj.core.api.Assertions.assertThat;

public class HistoricalChangesAffectedTestsSelectionExecutionFunctionalTest extends TestBedTemplate {

    @Test
    public void should_only_execute_tests_related_to_single_commit_in_business_logic_when_affected_is_enabled() throws Exception {
        // given
        project.configureSmartTesting()
                    .executionOrder(AFFECTED)
                    .inMode(SELECTING)
               .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsCommits("Single method body modification - sysout");

        // when
        final List<TestResult> actualTestResults = project.withProperties("git.commit", "HEAD", "git.previous.commit", "HEAD~").build();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_only_execute_tests_related_to_multiple_commits_in_business_logic_when_affected_is_enabled() throws Exception {
        // given
        project.configureSmartTesting()
                    .executionOrder(AFFECTED)
                    .inMode(SELECTING)
            .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsCommits("Single method body modification - sysout",
            "Inlined variable in a method");

        // when
        final List<TestResult> actualTestResults = project
            .withProperties("git.last.commits", "2")
            .build();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

}
