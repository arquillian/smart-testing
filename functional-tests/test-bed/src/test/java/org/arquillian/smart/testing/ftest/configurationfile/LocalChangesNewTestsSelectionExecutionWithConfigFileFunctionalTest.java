package org.arquillian.smart.testing.ftest.configurationfile;

import java.nio.file.Paths;
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
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalChangesNewTestsSelectionExecutionWithConfigFileFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_only_execute_new_tests_related_to_single_local_change() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                    .executionOrder(NEW)
                    .inMode(SELECTING)
                .createConfigFile()
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Adds new unit test");

        // when
        final TestResults actualTestResults = project.build().run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_load_configuration_from_parent_dir_if_not_present_in_current_execution_dir() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW)
                .inMode(SELECTING)
                .createConfigFile()
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Enable both surefire and failsafe plugin with sample integration test");

        // when
        final TestResults actualTestResults = project.build(Paths.get("core")).run("clean", "verify");

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }
}
