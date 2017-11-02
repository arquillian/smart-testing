package org.arquillian.smart.testing.ftest.configuration;

import org.arquillian.smart.testing.ftest.customAssertions.SmartTestingSoftAssertions;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.arquillian.smart.testing.hub.storage.local.DuringExecutionLocalStorage.SMART_TESTING_WORKING_DIRECTORY_NAME;

public class SkipTestExecutionFunctionalTest {

    private static final String SMART_TESTING_EXTENSION_DISABLED = "[INFO] Smart Testing Extension - Smart Testing is disabled.";
    private static final String SMART_TESTING_EXTENSION_ENABLED = "[INFO] Smart Testing Extension - Enabling extension.";
    private static final String[] CORE_MODULES = new String[] {"core/api", "core/spi", "core/impl-base"};

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Rule
    public final SmartTestingSoftAssertions softly = new SmartTestingSoftAssertions();

    @Test
    public void should_execute_only_unit_tests_when_integration_test_execution_is_skipped() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(ORDERING)
            .enable();

        project
            .applyAsLocalChanges("Enable both surefire and failsafe plugin with sample integration test");

        // when
        final TestResults actualTestResults = project
            .build(CORE_MODULES)
                .options()
                    .withSystemProperties("skipITs", "true")
                .configure()
            .run("clean", "verify");

        // then
        String capturedMavenLog = project.getMavenLog();
        softly.assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_ENABLED);
        softly.assertThat(actualTestResults.accumulatedPerTestClass()).size().isEqualTo(20);
    }

    @Test
    public void should_disable_smart_testing_and_execute_no_tests_when_test_execution_is_skipped() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(ORDERING)
            .enable();

        project
            .applyAsLocalChanges("Enable both surefire and failsafe plugin with sample integration test");

        // when
        final TestResults actualTestResults = project
            .build(CORE_MODULES)
                .options()
                    .skipTests(true)
                .configure()
            .run("clean", "verify");

        // then
        String capturedMavenLog = project.getMavenLog();
        softly.assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_DISABLED);
        softly.assertThat(actualTestResults.accumulatedPerTestClass()).size().isEqualTo(0);
    }

    @Test
    public void should_not_install_extension_when_only_clean_goal_is_used() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(NEW, AFFECTED)
            .inMode(SELECTING)
            .enable();

        // when
        project.build("config/impl-base").run("clean");

        // then
        softly.assertThat(project.getMavenLog()).contains(SMART_TESTING_EXTENSION_DISABLED);

        softly.assertThat(project)
            .doesNotContainDirectory(TemporaryInternalFiles.getScmChangesFileName())
            .doesNotContainDirectory(SMART_TESTING_WORKING_DIRECTORY_NAME)
            .doesNotContainDirectory("target");
    }

    @Test
    public void should_disable_smart_testing_and_execute_no_tests_when_skip_is_set_in_plugin_configuration_section_of_pom() throws Exception {
        // given
        final Project project = testBed.getProject();

        project
            .applyAsCommits("Configure skipTests property in plugin configuration section of pom");

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(ORDERING)
            .enable();

        // when
        final TestResults actualTestResults = project
            .build(CORE_MODULES)
                .options()
                    .withDebugOutput()
                    .logBuildOutput(false)
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        softly.assertThat(capturedMavenLog).contains("[DEBUG] Smart Testing Extension - Disabling Smart Testing ");
        softly.assertThat(actualTestResults.accumulatedPerTestClass()).size().isEqualTo(0);
    }

    @Test
    public void should_execute_only_unit_tests_when_skipITs_is_set_in_pom() throws Exception {
        // given
        final Project project = testBed.getProject();

        project
            .applyAsCommits("Configure skipITs as default property in pom");

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(ORDERING)
            .enable();

        // when
        final TestResults actualTestResults = project.build(CORE_MODULES).run("clean", "verify");

        // then
        String capturedMavenLog = project.getMavenLog();
        softly.assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_ENABLED);
        softly.assertThat(actualTestResults.accumulatedPerTestClass()).size().isEqualTo(20);
    }

    @Test
    public void should_execute_only_unit_tests_when_skipITs_is_set_as_custom_property_in_pom() throws Exception {
        // given
        final Project project = testBed.getProject();

        project
            .applyAsCommits("Configure skipITs as custom default property in pom");

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(ORDERING)
            .enable();

        // when
        final TestResults actualTestResults = project.build(CORE_MODULES).run("clean", "verify");

        // then
        String capturedMavenLog = project.getMavenLog();
        softly.assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_ENABLED);
        softly.assertThat(actualTestResults.accumulatedPerTestClass()).size().isEqualTo(20);
    }
}
