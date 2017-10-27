package org.arquillian.smart.testing.ftest.junit5;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.CHANGED;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalChangesTestsSelectionExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone("https://github.com/arquillian/smart-testing-junit5-demo.git");

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_execute_changed_tests() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(CHANGED)
            .inMode(SELECTING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsLocalChanges("changed: adds new test method");

        // when
        final TestResults actualTestResults = project.build().run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

}
