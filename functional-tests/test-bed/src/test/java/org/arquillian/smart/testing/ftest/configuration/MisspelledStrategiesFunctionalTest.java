package org.arquillian.smart.testing.ftest.configuration;

import org.arquillian.smart.testing.ftest.testbed.configuration.Strategy;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_DEBUG;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_AUTOCORRECT;
import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;
import static org.assertj.core.api.Assertions.assertThat;

public class MisspelledStrategiesFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_fail_and_log_suggestion_for_misspelled_strategies_when_autocorrect_disabled() throws Exception {
        // given
        final Strategy misspelledNew = new Strategy("neew");
        final Strategy misspelledAffected = new Strategy("affffected");

        final Project project = testBed.getProject();
        project.configureSmartTesting()
                .executionOrder(misspelledAffected, misspelledNew)
                .inMode(ORDERING)
            .enable();

        // when
        project.build("config/impl-base")
                .options()
                    .ignoreBuildFailure()
                    .withSystemProperties(SCM_RANGE_HEAD, "HEAD", SCM_RANGE_TAIL, "HEAD~", SMART_TESTING_DEBUG, "true")
                .configure()
            .run();

        // then
        String projectMavenLog = project.getMavenLog();
        assertThat(project.failed()).isTrue();
        assertThat(projectMavenLog).contains("Unable to find strategy [" + misspelledAffected.getName() + "]. Did you mean [affected]?");
        assertThat(projectMavenLog).contains("Unable to find strategy [" + misspelledNew.getName() + "]. Did you mean [new]?");
    }

    @Test
    public void should_succeed_for_misspelled_strategies_when_autocorrect_enabled() throws Exception {
        // given
        final Strategy misspelledNew = new Strategy("neew");
        final Strategy misspelledAffected = new Strategy("affffected");

        final Project project = testBed.getProject();
        project.configureSmartTesting()
            .executionOrder(misspelledAffected, misspelledNew)
            .inMode(ORDERING)
            .enable();

        // when
        project.build("config/impl-base")
            .options()
            .ignoreBuildFailure()
            .withSystemProperties(SMART_TESTING_DEBUG, "true", SMART_TESTING_AUTOCORRECT, "true")
            .configure()
            .run();

        // then
        assertThat(project.failed()).isFalse();
    }

}
