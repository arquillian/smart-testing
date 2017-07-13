package org.arquillian.smart.testing.ftest.simple;

import java.util.List;
import org.arquillian.smart.testing.ftest.TestBedTemplate;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
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
                .excludeProjects(modules)
                .withSystemProperties("smart.testing.disable", "true")
                .configure()
            .build();

        // then
        assertThat(actualTestResults).hasSize(74);
    }

}
