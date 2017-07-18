package org.arquillian.smart.testing.ftest.simple;

import java.util.List;
import org.arquillian.smart.testing.ftest.TestBedTemplate;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Criteria.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.assertj.core.api.Assertions.assertThat;

public class DisabledSmartTestingFunctionalTest extends TestBedTemplate {

    private static String[] modules = new String[] {"testng", "testng/core", "testng/container", "testng/standalone"};

    @Test
    public void should_execute_all_tests_when_smart_testing_is_disabled_irrespective_of_strategy() throws Exception {
        // given
        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(SELECTING)
            .enable();

        project
            .applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        final List<TestResult> actualTestResults = project
            .buildOptions()
                .withExcludeProjects(modules)
                .withSystemProperties("disableSmartTesting", "true")
                .configure()
            .build();

        // then
        assertThat(actualTestResults).hasSize(74);
    }

    @Test
    public void should_execute_selected_tests_when_smart_testing_is_enabled() throws Exception {
        // given
        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(SELECTING)
            .enable();

        project
            .applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        final List<TestResult> actualTestResults = project
            .buildOptions()
                .withExcludeProjects(modules)
                .withSystemProperties("git.last.commits", "2", "disableSmartTesting", "false")
                .configure()
            .build();

        // then
        assertThat(actualTestResults).hasSize(3);
    }

    @Test
    public void should_execute_selected_tests_based_on_provided_strategy_by_default() throws Exception {
        // given
        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(SELECTING)
            .enable();

        project
            .applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        final List<TestResult> actualTestResults = project
            .buildOptions()
                .withExcludeProjects(modules)
                .withSystemProperties("git.last.commits", "2")
                .configure()
            .build();

        // then
        assertThat(actualTestResults).hasSize(3);
    }
}
