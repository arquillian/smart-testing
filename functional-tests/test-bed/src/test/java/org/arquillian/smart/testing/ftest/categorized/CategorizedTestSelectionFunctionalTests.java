package org.arquillian.smart.testing.ftest.categorized;

import java.util.Collection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.ftest.testbed.configuration.builder.ConfigurationBuilder;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.arquillian.smart.testing.strategies.categorized.CategorizedConfiguration;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.CATEGORIZED;
import static org.assertj.core.api.Assertions.assertThat;

public class CategorizedTestSelectionFunctionalTests {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Test
    public void should_run_test_with_categories_loader_and_service() throws Exception {
        // given
        final Project project = testBed.getProject();
        CategorizedConfiguration categorizedConfiguration = new CategorizedConfiguration();
        categorizedConfiguration.setCategories(new String[] {"LoaderCategory", "serviceCategory"});
        Configuration config = new ConfigurationBuilder()
            .strategies(CATEGORIZED)
            .strategiesConfiguration()
            .add(categorizedConfiguration)
            .build()
            .build();

        project
            .configureSmartTesting()
            .executionOrder(CATEGORIZED)
            .inMode(SELECTING)
            .withConfiguration(config).createConfigFile()
            .enable();

        final Collection<TestResult> expectedTestResults =
            project.applyAsCommits("Added categories to core/impl-base");

        // when
        final TestResults actualTestResults =
            project.build("core/impl-base")
                .run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults)
            .hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_run_test_methods_with_categories_either_event_or_exception() throws Exception {
        // given
        final Project project = testBed.getProject();
        project
            .configureSmartTesting()
            .executionOrder(CATEGORIZED)
            .inMode(SELECTING)
            .enable();

        final Collection<TestResult> expectedTestResults =
            project.applyAsCommits("Added categories to test methods in core/impl-base");

        // when
        final TestResults actualTestResults =
            project.build("core/impl-base")
                .options()
                .withSystemProperties(
                    "smart.testing.categorized.categories", "exceptionCategory,EventCategory",
                    "test", "Event*,*Impl*,Observer*",
                    "failIfNoTests", "false")
                .configure()
                .run();

        // then
        assertThat(actualTestResults.getTestResults()).containsAll(expectedTestResults)
            .hasSameSizeAs(expectedTestResults);
    }
}
