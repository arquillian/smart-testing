package org.arquillian.smart.testing.ftest.changed;

import java.util.Collection;
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
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.CHANGED;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteAndRenameTestSelectionStrategyFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_run_renamed_test_as_changed_and_ignore_deleted_one() throws Exception {
        // given
        final Project project = testBed.getProject();
        project.configureSmartTesting().executionOrder(CHANGED).inMode(SELECTING).enable();

        final Collection<TestResult> expectedTestResults =
            project.applyAsCommits("Deletes one test", "Renames unit test");

        // when
        final TestResults actualTestResults =
            project.build("junit/core").options().withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~~").configure().run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults)
            .hasSameSizeAs(expectedTestResults);
    }
}
