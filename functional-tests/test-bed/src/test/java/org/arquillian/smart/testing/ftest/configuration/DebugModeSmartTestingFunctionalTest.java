package org.arquillian.smart.testing.ftest.configuration;

import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.ProjectBuilder;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_DEBUG;
import static org.arquillian.smart.testing.ftest.configuration.CustomAssertions.assertThatAllBuiltSubmodulesContainBuildArtifact;
import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;
import static org.assertj.core.api.Assertions.assertThat;

public class DebugModeSmartTestingFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());
    public static final String DEBUG_LOGS = "[DEBUG] [Smart Testing Extension]";

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
        final TestResults actualTestResults = projectBuilder
                .options()
                    .withSystemProperties(SCM_RANGE_HEAD, "HEAD", SCM_RANGE_TAIL, "HEAD~", SMART_TESTING_DEBUG, "true")
                .configure()
            .run();

        // then
        String projectMavenLog = project.getMavenLog();
        assertThat(projectMavenLog).contains(DEBUG_LOGS);
        assertThatAllBuiltSubmodulesContainBuildArtifact(projectBuilder.getBuiltProject(), "smart-testing/smart-testing-pom.xml");
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
        projectBuilder
                .options()
                    .withDebugOutput()
                    .withSystemProperties(SCM_RANGE_HEAD, "HEAD", SCM_RANGE_TAIL, "HEAD~")
                    .logBuildOutput(false)
                .configure()
            .run();

        // then
        String projectMavenLog = project.getMavenLog();
        assertThat(projectMavenLog).contains(DEBUG_LOGS);
        assertThatAllBuiltSubmodulesContainBuildArtifact(projectBuilder.getBuiltProject(), "smart-testing/smart-testing-pom.xml");
    }
}
