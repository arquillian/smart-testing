package org.arquillian.smart.testing.ftest.configuration;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.ProjectBuilder;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.Configuration.SMART_TESTING_DEBUG;
import static org.arquillian.smart.testing.LoggerConfigurator.SMART_TESTING_LOG_ENABLE;
import static org.arquillian.smart.testing.ftest.configuration.CustomAssertions.assertThatAllBuiltSubmodulesHaveReportsIncluded;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.COMMIT;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.PREVIOUS_COMMIT;
import static org.assertj.core.api.Assertions.assertThat;

public class DebugModeSmartTestingFunctionalTest {
    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_show_debug_logs_when_smart_testing_is_executed_in_debug_mode() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(ORDERING)
            .enable();

        project
            .applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        ProjectBuilder projectBuilder = project.build("config/impl-base");
        final Collection<TestResult> actualTestResults = projectBuilder
                .options()
                    .withSystemProperties(COMMIT, "HEAD", PREVIOUS_COMMIT, "HEAD~", SMART_TESTING_DEBUG, "true", "skipITs", "true")
                .configure()
            .run();

        // then
        String projectMavenLog = project.getMavenLog();
        assertThat(projectMavenLog).contains("DEBUG: Smart-Testing");
        assertThatAllBuiltSubmodulesHaveReportsIncluded(projectBuilder.getBuiltProject(), "smart-testing/smart-testing-pom.xml");
    }

    @Test
    public void should_show_debug_logs_when_maven_is_executed_in_debug_mode() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(AFFECTED)
            .inMode(ORDERING)
            .enable();

        project
            .applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        ProjectBuilder projectBuilder = project.build("config/impl-base");
        final Collection<TestResult> actualTestResults = projectBuilder
                .options()
                    .withDebugOutput()
                    .withSystemProperties(COMMIT, "HEAD", PREVIOUS_COMMIT, "HEAD~", "skipITs", "true")
                .configure()
            .run();

        // then
        String projectMavenLog = project.getMavenLog();
        assertThat(projectMavenLog).contains("DEBUG: Smart-Testing");
        assertThatAllBuiltSubmodulesHaveReportsIncluded(projectBuilder.getBuiltProject(), "smart-testing/smart-testing-pom.xml");
    }

    @Test
    public void should_show_and_store_debug_logs_when_debug_mode_is_set_with_file() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(AFFECTED)
            .inMode(ORDERING)
            .enable();

        project
            .applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        ProjectBuilder projectBuilder = project.build("config/impl-base");
        final Collection<TestResult> actualTestResults = projectBuilder
                .options()
                    .withSystemProperties(COMMIT, "HEAD", PREVIOUS_COMMIT, "HEAD~", SMART_TESTING_DEBUG, "true",  SMART_TESTING_LOG_ENABLE, "")
                .configure()
            .run();

        // then
        String projectMavenLog = project.getMavenLog();
        System.out.println(projectMavenLog);
        assertThat(projectMavenLog).contains("DEBUG: Smart-Testing");
        assertThatAllBuiltSubmodulesHaveReportsIncluded(projectBuilder.getBuiltProject(), "smart-testing-debug.log");
    }
}
