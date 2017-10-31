package org.arquillian.smart.testing.ftest.affected;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangesOnDifferentModulesAffectedTestsSelectionExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_detect_changes_on_maven_modules_and_execute_test() {
        // given
        final Project project = testBed.getProject();
        project.configureSmartTesting()
            .executionOrder(AFFECTED)
            .inMode(SELECTING)
            .enable();

        final Collection<TestResult> expectedTestResults = project.applyAsCommits("Adds class in one module to affect test in another");

        // when
        final TestResults actualTestResults = project
            .build("config/impl-base")
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~", "smart.testing.apply.to", "surefire")
                .configure()
            .run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).hasSameSizeAs(expectedTestResults).containsAll(expectedTestResults);
    }

    @Test
    @Ignore("Example of debugging option as living documentation")
    public void should_detect_changes_on_maven_modules_and_execute_test_in_debug_mode() {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(AFFECTED)
            .inMode(SELECTING)
            .enable();

        final Collection<TestResult> expectedTestResults = project.applyAsCommits("Adds class in one module to affect test in another");

        // when
        // tag::documentation_test_bed_debug[]
        final TestResults actualTestResults = project
            .build()
            .options()
                .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~")
                .withDebugOutput()
                .withRemoteDebugging()
                .withRemoteSurefireDebugging()
            .configure()
            .run();
        // end::documentation_test_bed_debug[]

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).hasSameSizeAs(expectedTestResults).containsAll(expectedTestResults);
    }

}
