package org.arquillian.smart.testing.ftest.configuration;

import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class SkipTestExecutionFunctionalTest {

    private static final String SMART_TESTING_EXTENSION_DISABLED = "[Smart Testing Extension] Smart Testing is disabled.";
    private static final String SMART_TESTING_EXTENSION_ENABLED = "[Smart Testing Extension] Enabling extension.";
    private static final String[] CORE_MODULES = new String[] {"core/api", "core/spi", "core/impl-base"};

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_execute_all_unit_tests_when_integration_test_execution_is_skipped() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(ORDERING)
            .enable();

        // when
        final TestResults actualTestResults = project
            .build(CORE_MODULES)
                .options()
                    .withSystemProperties("skipITs", "true")
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_ENABLED);
        assertThat(actualTestResults.accumulatedPerTestClass()).size().isEqualTo(20);
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
        final TestResults actualTestResults = project
            .build(CORE_MODULES)
                .options()
                    .skipTests(true)
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_DISABLED);
        assertThat(actualTestResults.accumulatedPerTestClass()).size().isEqualTo(0);
    }
}
