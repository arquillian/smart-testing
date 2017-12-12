package org.arquillian.smart.testing.ftest.configurationfile;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.customAssertions.SmartTestingSoftAssertions;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.FAILED;

public class HistoricalChangesFailedTestsSelectionExecutionWithConfigFileFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Rule
    public final SmartTestingSoftAssertions softly = new SmartTestingSoftAssertions();

    @Test
    public void should_only_execute_previously_failing_tests_when_failed_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.applyAsCommits("Introduces error by changing return value");

        project.build()
                .options()
                    .ignoreBuildFailure()
                .configure()
            .run();

        // tag::documentation[]
        project.configureSmartTesting()
                    .executionOrder(FAILED)
                    .inMode(SELECTING)
                .createConfigFile()
            .enable();
        // end::documentation[]

        final Collection<TestResult> expectedTestResults = project.applyAsCommits("fix: Introduces error by changing return value");

        // when
        final TestResults actualTestResults = project.build()
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~")
                .configure()
            .run();

        // then
        softly.assertThat(actualTestResults.accumulatedPerTestClass())
            .containsAll(expectedTestResults)
            .hasSameSizeAs(expectedTestResults);

        softly.assertThat(project).doesNotContainDirectory(TemporaryInternalFiles.getTestReportDirName());
    }
}
