package org.arquillian.smart.testing.ftest.configuration;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.ProjectBuilder;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_REPORT_ENABLE;
import static org.arquillian.smart.testing.ftest.configuration.CustomAssertions.assertThatAllBuiltSubmodulesContainBuildArtifact;
import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class SurefireForksConfigurationTest {

    private static final String SUREFIRE_PROVIDER_LOGS = "Smart Testing Extension -";

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

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
        ProjectBuilder projectBuilder = project.build("config/impl-base");
        final TestResults actualTestResults = projectBuilder
            .options()
                .withSystemProperties(systemPropertiesPairs)
                .withSystemProperties(SMART_TESTING_REPORT_ENABLE, "true")
            .configure()
            .run();

        // then
        String projectMavenLog = project.getMavenLog();
        assertThat(projectMavenLog).contains(SUREFIRE_PROVIDER_LOGS);
        softly.assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
        assertThatAllBuiltSubmodulesContainBuildArtifact(projectBuilder.getBuiltProject(), REPORT_FILE_NAME);
    }
}
