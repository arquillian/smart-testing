package org.arquillian.smart.testing.ftest.configuration;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.assertj.core.api.Assertions.assertThat;

public class SpecificTestClassExecutionFunctionalTest {

    private static final String SMART_TESTING_EXTENSION_DISABLED = "[INFO] Smart Testing Extension - Smart Testing is disabled.";
    private static final String SMART_TESTING_EXTENSION_ENABLED = "[INFO] Smart Testing Extension - Enabling extension.";
    private static final String BASE_CONFIG = "config/impl-base";

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_disable_extension_and_only_execute_specific_test_when_single_test_set() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW)
                .inMode(ORDERING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Adds new unit test");

        // when
        final TestResults actualTestResults = project
            .build(BASE_CONFIG)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~", "test", "PropertiesParserTestCase", "failIfNoTests", "false")
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_DISABLED);
        assertThat(actualTestResults.accumulatedPerTestClass()).doesNotContainAnyElementsOf(expectedTestResults).hasSize(1);
    }

    @Test
    public void should_disable_extension_and_only_execute_specified_tests_when_tests_without_pattern_set() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW)
                .inMode(ORDERING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Adds new unit test");

        // when
        final TestResults actualTestResults = project
            .build(BASE_CONFIG)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~",
                        "test", "PropertiesParserTestCase, ConfigurationRegistrarTestCase", "failIfNoTests", "false")
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_DISABLED);
        assertThat(actualTestResults.accumulatedPerTestClass()).doesNotContainAnyElementsOf(expectedTestResults).hasSize(2);
    }

    @Test
    public void should_not_disable_extension_execute_all_specified_tests_when_tests_with_pattern_set() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW)
                .inMode(ORDERING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Adds new unit test");

        // when
        final TestResults actualTestResults = project
            .build(BASE_CONFIG)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~",
                        "test", "*Properties*, ConfigurationRegistrarTestCase", "failIfNoTests", "false")
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_ENABLED);
        assertThat(actualTestResults.accumulatedPerTestClass()).containsSequence(expectedTestResults).hasSize(4);
    }

    @Test
    public void should_execute_specified_tests_with_ordering_mode_in_order_defined_by_strategy_used() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW)
                .inMode(ORDERING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Adds new unit test");

        // when
        final TestResults actualTestResults = project
            .build(BASE_CONFIG)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~", "test", "*Properties*", "failIfNoTests", "false")
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_ENABLED);
        assertThat(actualTestResults.accumulatedPerTestClass()).containsSequence(expectedTestResults).hasSize(3);
    }

    @Test
    public void should_not_execute_any_test_when_test_class_pattern_used_with_selecting_mode_does_not_fit_strategy() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW)
                .inMode(SELECTING)
            .enable();

        project
            .applyAsCommits("Adds new unit test");

        // when
        final TestResults actualTestResults = project
            .build(BASE_CONFIG)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~", "test", "Properties*")
                .configure()
            .run();

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_ENABLED);
        assertThat(actualTestResults.accumulatedPerTestClass()).isEmpty();
    }

   @Test
    public void should_only_execute_test_common_with_selected_strategy_when_test_pattern_used_with_selecting_mode()
        throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(SELECTING)
            .enable();

        project
            .applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        final TestResults actualTestResults = project
            .build(BASE_CONFIG)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~", "test", "Configuration*", "failIfNoTests", "false")
                .configure()
            .run();

        // then
       String capturedMavenLog = project.getMavenLog();
       assertThat(capturedMavenLog).contains(SMART_TESTING_EXTENSION_ENABLED);
       assertThat(actualTestResults.accumulatedPerTestClass()).extracting("className")
           .containsOnly("org.jboss.arquillian.config.impl.extension.ConfigurationRegistrarTestCase");
    }
}
