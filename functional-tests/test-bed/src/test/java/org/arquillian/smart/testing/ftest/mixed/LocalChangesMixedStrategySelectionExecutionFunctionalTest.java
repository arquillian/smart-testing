package org.arquillian.smart.testing.ftest.mixed;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.FileVerifier;
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
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.arquillian.smart.testing.hub.storage.local.DuringExecutionLocalStorage.SMART_TESTING_WORKING_DIRECTORY_NAME;
import static org.arquillian.smart.testing.hub.storage.local.LocalChangeStorage.SMART_TESTING_SCM_CHANGES;
import static org.assertj.core.api.Assertions.assertThat;

// tag::documentation[]
public class LocalChangesMixedStrategySelectionExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_execute_all_new_tests_and_related_to_production_code_changes() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW, AFFECTED)
                .inMode(SELECTING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Single method body modification - sysout",
                "Inlined variable in a method", "Adds new unit test");

        // when
        final TestResults actualTestResults = project.build().run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
        FileVerifier.assertThatFileIsNotPresent(project, SMART_TESTING_SCM_CHANGES);
        FileVerifier.assertThatFileIsNotPresent(project, SMART_TESTING_WORKING_DIRECTORY_NAME);
    }
}
// end::documentation[]
