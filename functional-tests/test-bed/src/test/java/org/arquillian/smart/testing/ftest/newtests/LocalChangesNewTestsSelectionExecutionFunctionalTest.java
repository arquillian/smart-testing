package org.arquillian.smart.testing.ftest.newtests;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalChangesNewTestsSelectionExecutionFunctionalTest {

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
               .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Adds new unit test");

        // when
        final TestResults actualTestResults = project.build("config/impl-base").run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_only_execute_new_tests_related_to_single_local_change_using_failsafe() {

        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(NEW)
            .inMode(SELECTING)
            .enable();

        project
            .applyAsCommits("Disable surefire and enable just failsafe plugin");

        final Collection<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Adds new unit test");

        // when
        final TestResults actualTestResults = project.build("config/impl-base").run("clean", "verify");

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);

    }

    @Test
    public void should_only_execute_new_tests_when_multiple_local_changes_applied() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                    .executionOrder(NEW)
                    .inMode(SELECTING)
            .enable();

        // we ignore expected tests results for this commits, as they are changes to existing ones,
        // but still want to apply the changes

        // tag::documentation_apply[]
        project.applyAsLocalChanges("Single method body modification - sysout",
            "Inlined variable in a method");
        // end::documentation_apply[]

        final Collection<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Adds new unit test");

        // when
        // tag::documentation_build[]
        final TestResults actualTestResults = project.build().run();
        // end::documentation_build[]

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

}
