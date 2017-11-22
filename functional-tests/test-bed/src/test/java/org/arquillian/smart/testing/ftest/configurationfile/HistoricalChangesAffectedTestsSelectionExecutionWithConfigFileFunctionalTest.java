package org.arquillian.smart.testing.ftest.configurationfile;

import java.util.Collection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.ftest.testbed.configuration.builder.ConfigurationBuilder;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class HistoricalChangesAffectedTestsSelectionExecutionWithConfigFileFunctionalTest {
    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_only_execute_tests_related_to_single_commit_in_business_logic_when_affected_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        final String customConfigFile = "core/custom-config-file.yml";

        final Configuration configuration = new ConfigurationBuilder()
                .mode(SELECTING)
                .strategies(AFFECTED.getName())
                .scm()
                    .range()
                        .head("HEAD").tail("HEAD~")
                        .build()
                    .build()
                .build();

        project.configureSmartTesting()
                    .withConfiguration(configuration)
                .createConfigFile(customConfigFile)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Single method body modification - sysout");

        // when
        final TestResults actualTestResults = project.build("config/impl-base")
                .options()
                    .withSystemProperties("smart.testing.config", customConfigFile)
                .configure()
            .run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_only_execute_tests_related_to_multiple_commits_in_business_logic_when_affected_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        // tag::documentation_config[]
        final Configuration configuration = new ConfigurationBuilder()
                .mode(SELECTING)
                .strategies(AFFECTED)
                .scm()
                    .lastChanges("2")
                    .build()
                .build();
        // end::documentation_config[]

        // tag::documentation[]
        project.configureSmartTesting()
                    .withConfiguration(configuration)
                .createConfigFile()
            .enable();
        // end::documentation[]


        final Collection<TestResult> expectedTestResults = project.applyAsCommits("Single method body modification - sysout",
                "Inlined variable in a method");

        // when
        final TestResults actualTestResults = project.build().run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }
}
