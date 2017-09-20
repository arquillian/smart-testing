package org.arquillian.smart.testing.ftest.configuration;

import org.arquillian.smart.testing.ftest.testbed.configuration.Strategy;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.Configuration.SMART_TESTING_DEBUG;
import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.COMMIT;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.PREVIOUS_COMMIT;
import static org.assertj.core.api.Assertions.assertThat;

public class MisspelledStrategiesFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_fail_and_log_suggestion_for_misspelled_strategies() throws Exception {
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
                    .withSystemProperties(COMMIT, "HEAD", PREVIOUS_COMMIT, "HEAD~", SMART_TESTING_DEBUG, "true")
                .configure()
            .run();

        // then
        String projectMavenLog = project.getMavenLog();
        assertThat(project.failed()).isTrue();
        assertThat(projectMavenLog).contains("Unable to find strategy [" + misspelledAffected.getName() + "]. Did you mean [affected]?");
        assertThat(projectMavenLog).contains("Unable to find strategy [" + misspelledNew.getName() + "]. Did you mean [new]?");
    }

}
