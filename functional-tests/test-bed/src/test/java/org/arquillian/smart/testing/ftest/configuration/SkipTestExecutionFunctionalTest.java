package org.arquillian.smart.testing.ftest.configuration;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class SkipTestExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    private static final String EXPECTED_LOG_PART = "Enabling Smart Testing";

    private static final String[] modules = new String[] {"core/api", "core/spi", "core/impl-base"};

    @Test
    public void should_execute_all_unit_tests_when_integration_test_execution_is_skipped() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(ORDERING)
            .enable();

        // when
        final Collection<TestResult> actualTestResults = project
            .build(modules)
                .options()
                    .withSystemProperties("skipITs", "true")
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(EXPECTED_LOG_PART);
        assertThat(actualTestResults).size().isEqualTo(20);
    }

    @Test
    public void should_disable_smart_testing_and_execute_no_tests_when_test_execution_is_skipped() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(ORDERING)
            .enable();

        // when
        final Collection<TestResult> actualTestResults = project
            .build(modules)
                .options()
                    .skipTests(true)
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).doesNotContain(EXPECTED_LOG_PART);
        assertThat(actualTestResults).size().isEqualTo(0);
    }
}
