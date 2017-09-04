package org.arquillian.smart.testing.ftest.configuration;

import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Collection;
import java.util.List;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.assertj.core.api.Assertions.assertThat;

public class SurefireForksConfigurationTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    @Rule
    public TestName name= new TestName();

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
    @Ignore("FIXME https://github.com/arquillian/smart-testing/issues/124")
    public void test_with_multiple_forks() {
        verifyTestSuiteExecution("forkCount", "2");
    }

    @Test
    public void test_with_multiple_forks_not_reusing_forks() {
        verifyTestSuiteExecution("forkCount", "4", "reuseForks", "false");
    }

    @Test
    public void test_with_fork_count_zero_not_reusing_forks() {
        verifyTestSuiteExecution("forkCount", "0", "reuseForks", "false");
    }

    private void verifyTestSuiteExecution(String... systemPropertiesPairs){
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(AFFECTED, NEW)
            .inMode(SELECTING)
            .enable();

        Collection<TestResult> expectedTestResults = project
            .applyAsLocalChanges("Inlined variable in a method", "Adds new unit test", "fixes tests");

        // when
        final List<TestResult> actualTestResults =
            project
                .build()
                .options()
                    //.withRemoteDebugging()
                    //.withRemoteSurefireDebugging()
                    .withSystemProperties(systemPropertiesPairs)
                    .withSystemProperties("graph.name", name.getMethodName())
                    .withSystemProperties("smart.testing.debug", "true") // This will only be propagated to surefire for "not_reusing_forks" option
                    .configure()
                .run();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }
}
