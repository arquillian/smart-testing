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
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.assertj.core.api.Assertions.assertThat;

public class SpecificTestClassExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    private static final String module = "config/impl-base";

    private static final String EXPECTED_LOG_PART = "Enabling Smart Testing";

    @Test
    public void should_disable_extension_and_only_execute_specified_test_when_single_test_set() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW)
                .inMode(ORDERING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Adds new unit test");

        // when
        final Collection<TestResult> actualTestResults = project
            .build(module)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~", "test", "PropertiesParserTestCase", "failIfNoTests", "false")
                .configure()
            .run("clean", "test");

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).doesNotContain(EXPECTED_LOG_PART);
        assertThat(actualTestResults).doesNotContainAnyElementsOf(expectedTestResults).hasSize(1);
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
        final Collection<TestResult> actualTestResults = project
            .build(module)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~", "test", "*Properties*", "failIfNoTests", "false")
                .configure()
            .run("clean", "test");

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(EXPECTED_LOG_PART);
        assertThat(actualTestResults).containsSequence(expectedTestResults).hasSize(3);
    }

    @Test
    public void should_not_execute_any_test_when_test_class_used_with_selecting_mode_does_not_fit_strategy() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(NEW)
                .inMode(SELECTING)
            .enable();

        project
            .applyAsCommits("Adds new unit test");

        // when
        final Collection<TestResult> actualTestResults = project
            .build(module)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~", "test", "Properties*", "failIfNoTests", "false")
                    .ignoreBuildFailure()
                .configure()
            .run("clean", "test");

        // then
        String capturedMavenLog = project.getMavenLog();
        assertThat(capturedMavenLog).contains(EXPECTED_LOG_PART);
        assertThat(actualTestResults).isEmpty();
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

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        final Collection<TestResult> actualTestResults = project
            .build(module)
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~", "test", "Configuration*", "failIfNoTests", "false")
                .configure()
            .run("clean", "test");

        // then
       String capturedMavenLog = project.getMavenLog();
       assertThat(capturedMavenLog).contains(EXPECTED_LOG_PART);
       assertThat(actualTestResults).isSubsetOf(expectedTestResults).hasSize(1);
    }
}
