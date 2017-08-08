package org.arquillian.smart.testing.ftest.configuration;

import java.util.List;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.assertj.core.api.Assertions.assertThat;

public class SurefireForksConfigurationTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void test_with_reuse_forks_false() {
        verifyTestSuiteExecution("reuseForks", "false");
    }

    @Test
    public void test_with_fork_count_zero() {
        verifyTestSuiteExecution("forkCount", "0");
    }

    @Test
    public void test_with_fork_count_one() {
        verifyTestSuiteExecution("forkCount", "1");
    }

    @Test
    public void test_with_fork_count_ten() {
        verifyTestSuiteExecution("forkCount", "10");
    }

    @Test
    public void test_with_fork_count_ten_not_reusing_forks() {
        verifyTestSuiteExecution("forkCount", "10", "reuseForks", "false");
    }

    @Test
    public void test_with_fork_count_zero_not_reusing_forks() {
        verifyTestSuiteExecution("forkCount", "0", "reuseForks", "false");
    }


    private void verifyTestSuiteExecution(String... systemPropertiesPairs){
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(NEW, AFFECTED)
            .inMode(SELECTING)
            .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Inlined variable in a method", "Adds new unit test");

        // when
        final List<TestResult> actualTestResults =
            project
                .build()
                .options()
                .withSystemProperties(systemPropertiesPairs).configure()
                .run();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }
}
