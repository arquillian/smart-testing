package org.arquillian.smart.testing.ftest.affected;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.customAssertions.SmartTestingSoftAssertions;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class HistoricalChangesAffectedTestsSelectionExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Rule
    public final SmartTestingSoftAssertions softly = new SmartTestingSoftAssertions();

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
        final TestResults actualTestResults = project
            .build()
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~")
                .configure()
            .run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
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
        final TestResults actualTestResults = project
            .build()
                .options()
                    .withSystemProperties("scm.last.changes", "2")
                .configure()
            .run();

        // then
        softly.assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);

    }
}
