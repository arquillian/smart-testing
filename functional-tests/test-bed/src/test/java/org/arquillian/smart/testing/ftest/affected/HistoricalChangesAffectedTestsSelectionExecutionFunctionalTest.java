package org.arquillian.smart.testing.ftest.affected;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class HistoricalChangesAffectedTestsSelectionExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_only_execute_tests_related_to_single_commit_in_business_logic_when_affected_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                    .executionOrder(AFFECTED)
                    .inMode(SELECTING)
               .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Single method body modification - sysout");

        // when
        final Collection<TestResult> actualTestResults = project
            .build()
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~")
                .configure()
            .run();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_only_execute_tests_related_to_multiple_commits_in_business_logic_when_affected_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                    .executionOrder(AFFECTED)
                    .inMode(SELECTING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Single method body modification - sysout",
            "Inlined variable in a method");

        // when
        final Collection<TestResult> actualTestResults = project
            .build()
                .options()
                    .withSystemProperties("scm.last.changes", "2")
                .configure()
            .run();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

}
