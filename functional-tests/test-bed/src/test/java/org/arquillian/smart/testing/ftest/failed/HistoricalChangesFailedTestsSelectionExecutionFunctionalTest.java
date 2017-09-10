package org.arquillian.smart.testing.ftest.failed;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.failed.TestReportHandler.copySurefireReports;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.FAILED;
import static org.assertj.core.api.Assertions.assertThat;

public class HistoricalChangesFailedTestsSelectionExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_only_execute_previously_failing_tests_when_failed_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.applyAsCommits("Introduces error by changing return value");

        project
            .build()
                .options()
                    .ignoreBuildFailure()
                .configure()
            .run();

        project.configureSmartTesting()
                .executionOrder(FAILED)
                .inMode(SELECTING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("fix: Introduces error by changing return value");

        copySurefireReports(project);

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
}
