package org.arquillian.smart.testing.ftest.newtests;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.assertj.core.api.Assertions.assertThat;

public class HistoricalChangesNewTestsSelectionExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_only_execute_newly_added_tests_if_new_strategy_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        // tag::documentation[]
        project.configureSmartTesting()
                    .executionOrder(NEW)
                    .inMode(SELECTING)
               .enable();
        // end::documentation[]

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Adds new unit test");

        // when
        final Collection<TestResult> actualTestResults = project
            .build()
                .options()
                    .withSystemProperties("git.commit", "HEAD", "git.previous.commit", "HEAD~")
                .configure()
            .run();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_only_execute_new_test_when_multiple_commits_with_changed_tests_applied() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                    .executionOrder(NEW)
                    .inMode(SELECTING)
            .enable();

        // we ignore expected tests results for this commits, as they are changes to existing ones,
        // but still want to apply the changes
        project.applyAsCommits("Single method body modification - sysout",
            "Inlined variable in a method");

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Adds new unit test");

        // when
        final Collection<TestResult> actualTestResults = project
            .build()
                .options()
                    .withSystemProperties("git.last.commits", "3")
                .configure()
            .run();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }
}
