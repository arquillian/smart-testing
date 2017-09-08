package org.arquillian.smart.testing.ftest.configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.Status;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.ORDERING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.CHANGED;
import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.SKIPPED;
import static org.assertj.core.api.Assertions.assertThat;

public class SkipAfterFailureCountTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_skip_all_remaining_tests_with_first_failure_when_ordering_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(CHANGED)
            .inMode(ORDERING)
            .enable();

        project.applyAsCommits("Introduces error by changing return value");

        // when
        final Map<Status, Long> resultCount = new LinkedHashMap<>();

        project.build("container/impl-base")
            .options()
                .ignoreBuildFailure()
            .configure()
            .run(resultCount);


        // then
        final Long skippedCount = resultCount.get(SKIPPED);
        assertThat(skippedCount).isGreaterThan(4);
    }

    @Test
    public void should_override_skip_after_failure_count_system_property_when_ordering_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
            .executionOrder(CHANGED)
            .inMode(ORDERING)
            .enable();

        project.applyAsCommits("Introduces error by changing return value");

        // when
        final Map<Status, Long> resultStatusCount = new LinkedHashMap<>();
        project.build("container/impl-base")
            .options()
                .withSystemProperties("surefire.skipAfterFailureCount", "0")
                .ignoreBuildFailure()
            .configure()
            .run(resultStatusCount);

        // then
        final Long skippedCount = resultStatusCount.get(SKIPPED);
        assertThat(skippedCount).isEqualTo(4);
    }
}
