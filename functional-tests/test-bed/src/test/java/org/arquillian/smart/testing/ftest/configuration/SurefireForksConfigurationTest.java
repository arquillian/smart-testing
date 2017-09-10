package org.arquillian.smart.testing.ftest.configuration;

import java.io.File;
import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.ProjectBuilder;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.assertj.core.api.FileAssert;
import org.assertj.core.api.JUnitSoftAssertions;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.DEFAULT_REPORT_FILE_NAME;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.ENABLE_REPORT_PROPERTY;

public class SurefireForksConfigurationTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

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
                .withSystemProperties(ENABLE_REPORT_PROPERTY, "true")
            .configure()
            .run();

        // then
        softly.assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
        assertThatAllBuiltSubmodulesHaveReportsIncluded(projectBuilder.getBuiltProject());
    }

    private void assertThatAllBuiltSubmodulesHaveReportsIncluded(BuiltProject module) {
        module.getModules().forEach(this::assertThatReportFileIsIncludedIn);
    }

    private void assertThatReportFileIsIncludedIn(BuiltProject subModule) {
        final File targetDirectory = subModule.getTargetDirectory();
        final FileAssert fileAssert = softly.assertThat(new File(targetDirectory, DEFAULT_REPORT_FILE_NAME));
        if (isJar(subModule)) {
            if (testsWereExecuted(targetDirectory)) {
                fileAssert.exists();
            } else {
                fileAssert.doesNotExist();
            }
        } else {
            assertThatAllBuiltSubmodulesHaveReportsIncluded(subModule);
            fileAssert.doesNotExist();
        }
    }

    private boolean isJar(BuiltProject subModule) {
        return subModule.getModel().getPackaging().equals("jar");
    }

    private boolean testsWereExecuted(File target) {
        return new File(target, "test-classes").exists();
    }
}
