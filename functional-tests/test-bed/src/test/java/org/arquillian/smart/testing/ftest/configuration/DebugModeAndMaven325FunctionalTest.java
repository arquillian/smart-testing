package org.arquillian.smart.testing.ftest.configuration;

import java.util.Collection;
import org.arquillian.smart.testing.ftest.customAssertions.SmartTestingSoftAssertions;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.ProjectBuilder;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.project.Using;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_DEBUG;
import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.mvn.ext.ModifiedPomExporter.SMART_TESTING_POM_FILE;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;

public class DebugModeAndMaven325FunctionalTest {

    private static final String MAVEN_DEBUG_LOGS = "[DEBUG] Smart Testing Extension -";
    private static final String PROVIDER_DEBUG_LOGS = "DEBUG: Smart Testing Extension -";

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    @Rule
    public final SmartTestingSoftAssertions softly = new SmartTestingSoftAssertions();

    @Test
    public void should_show_debug_logs_and_generate_report_when_smart_testing_is_executed_in_debug_mode_changes_using_325_maven()
        throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(SELECTING)
            .enable(Using.SHADED_JAR);

        Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        ProjectBuilder projectBuilder = project.build("config/impl-base");
        final TestResults actualTestResults = projectBuilder
                .options()
                    .withSystemProperties(SCM_LAST_CHANGES, "2", SMART_TESTING_DEBUG, "true")
                    .useMavenVersion("3.2.5")
                .configure()
            .run();

        // then
        softly.assertThat(actualTestResults.accumulatedPerTestClass())
            .containsAll(expectedTestResults)
            .hasSameSizeAs(expectedTestResults);

        String projectMavenLog = project.getMavenLog();

        softly.assertThat(projectMavenLog)
            .contains(PROVIDER_DEBUG_LOGS)
            .contains(PROVIDER_DEBUG_LOGS + " Applied user properties");

        softly.assertThat(projectBuilder.getBuiltProject()).allBuiltSubModulesContainEffectivePom(SMART_TESTING_POM_FILE);
        softly.assertThat(projectBuilder.getBuiltProject()).allBuiltSubModulesWithTestExecutionsContainReport(REPORT_FILE_NAME);
    }

    @Test
    public void should_show_debug_logs_and_generate_report_when_maven_is_executed_in_debug_mode() throws Exception {
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

        softly.assertThat(projectMavenLog)
            .contains(MAVEN_DEBUG_LOGS)
            .contains(PROVIDER_DEBUG_LOGS);

        softly.assertThat(projectBuilder.getBuiltProject()).allBuiltSubModulesContainEffectivePom(SMART_TESTING_POM_FILE);
        softly.assertThat(projectBuilder.getBuiltProject()).allBuiltSubModulesWithTestExecutionsContainReport(REPORT_FILE_NAME);
    }
}
