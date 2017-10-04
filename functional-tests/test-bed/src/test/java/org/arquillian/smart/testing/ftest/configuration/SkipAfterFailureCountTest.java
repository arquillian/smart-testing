package org.arquillian.smart.testing.ftest.configuration;

import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.configuration.assertions.TestResultAssert.assertThat;
import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.CHANGED;
import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.SKIPPED;

public class SkipAfterFailureCountTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_not_execute_remaining_tests_after_first_failure_as_skipAfterFailureCount_set_to_one_by_smart_testing_when_ordering_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(CHANGED)
            .inMode(ORDERING)
            .enable();

        project.applyAsCommits("Introduces error by changing return value");

        // when
        final TestResults testResults = project.build("container/impl-base")
            .options()
                .ignoreBuildFailure()
            .configure()
            .run();

        // then
        assertThat(testResults.testsWithStatuses(SKIPPED))
            .hasSkippedClasses(
                "org.jboss.arquillian.container.impl.client.container.ContainerLifecycleControllerTestCase",
                "org.jboss.arquillian.container.impl.client.container.DeploymentExceptionHandlerTestCase",
                "org.jboss.arquillian.container.impl.client.container.ContainerRegistryCreatorTestCase",
                "org.jboss.arquillian.container.impl.client.container.ContainerDeployControllerTestCase",
                "org.jboss.arquillian.container.impl.client.deployment.ArchiveDeploymentExporterTestCase"
            );
    }

    @Test
    public void should_execute_remaining_tests_after_failure_as_skipAfterFailureCount_set_to_zero_with_system_property_when_ordering_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(CHANGED)
            .inMode(ORDERING)
            .enable();

        project.applyAsCommits("Introduces error by changing return value");

        // when
        final TestResults testResults = project.build("container/impl-base")
            .options()
                .withSystemProperties("surefire.skipAfterFailureCount", "0")
                .ignoreBuildFailure()
            .configure()
            .run();

        // then
        assertThat(testResults.testsWithStatuses(SKIPPED)).doesNotHaveSkippedClasses(
                "org.jboss.arquillian.container.impl.client.container.ContainerLifecycleControllerTestCase",
                "org.jboss.arquillian.container.impl.client.container.DeploymentExceptionHandlerTestCase",
                "org.jboss.arquillian.container.impl.client.container.ContainerRegistryCreatorTestCase",
                "org.jboss.arquillian.container.impl.client.container.ContainerDeployControllerTestCase",
                "org.jboss.arquillian.container.impl.client.deployment.ArchiveDeploymentExporterTestCase"
            );
    }
}
