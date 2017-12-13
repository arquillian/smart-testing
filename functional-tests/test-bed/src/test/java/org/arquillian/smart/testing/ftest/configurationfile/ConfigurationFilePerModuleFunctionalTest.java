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
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.CHANGED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.NEW;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationFilePerModuleFunctionalTest {
    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_load_config_from_module_config_file_for_local_changes_instead_of_parent_config_with_scm_and_strategy() {
        // given
        final Project project = testBed.getProject();

        final Configuration parentConfiguration = new ConfigurationBuilder()
                .strategies(NEW)
                .scm()
                    .lastChanges("2")
                .build()
            .build();


        final Configuration changedConfiguration = new ConfigurationBuilder()
                .strategies(CHANGED)
                .mode(SELECTING)
            .build();

        project.configureSmartTesting()
                .withConfiguration(parentConfiguration)
                    .createConfigFile()
                .withConfiguration(changedConfiguration)
                    .createConfigFileIn("junit/core")
            .enable();


        final Collection<TestResult> expectedTestResults = project.applyAsCommits("Deletes one test", "Renames unit test");

        // when
        final TestResults actualTestResults =
            project.build("junit/core").run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults)
            .hasSameSizeAs(expectedTestResults);
    }
}
