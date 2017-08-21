package org.arquillian.smart.testing.ftest.configuration;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class DisabledSmartTestingFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    private static String[] modules = new String[] {"testng", "testng/core", "testng/container", "testng/standalone"};

    @Test
    public void should_execute_all_tests_when_smart_testing_is_disabled_irrespective_of_strategy() throws Exception {
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
        final Collection<TestResult> actualTestResults = project
            .build()
                .options()
                    .excludeProjects(modules)
                    .withSystemProperties("smart.testing.disable", "true")
                .configure()
            .run();

        // then
        assertThat(actualTestResults).hasSize(74);
    }
}
