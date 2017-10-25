package org.arquillian.smart.testing.ftest.affected;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.RunMode;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.SmartTesting;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.ftest.customAssertions.SmartTestingSoftAssertions;
import org.arquillian.smart.testing.ftest.newtests.HistoricalChangesNewTestsSelectionExecutionFunctionalTest;
import org.arquillian.smart.testing.ftest.testbed.ProjectPersistTest;
import org.arquillian.smart.testing.ftest.testbed.ProjectPersistUsingPropertyTest;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.project.TestResults;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.rules.TestBed;
import org.arquillian.smart.testing.rules.git.GitClone;
import org.arquillian.smart.testing.strategies.affected.AffectedConfiguration;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.TestRepository.testRepository;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class HistoricalChangesAffectedTestsSelectionExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone(testRepository());

    @Rule
    public final TestBed testBed = new TestBed(GIT_CLONE);

    @Rule
    public final SmartTestingSoftAssertions softly = new SmartTestingSoftAssertions();

    @Test
    public void should_only_execute_tests_related_to_single_commit_in_business_logic_when_affected_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                    .executionOrder(AFFECTED)
                    .inMode(SELECTING)
               .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Single method body modification - sysout");

        // when
        final TestResults actualTestResults = project
            .build()
                .options()
                    .withSystemProperties("scm.range.head", "HEAD", "scm.range.tail", "HEAD~")
                .configure()
            .run();

        // then
        assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }

    @Test
    public void should_only_execute_tests_related_to_multiple_commits_in_business_logic_when_affected_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                    .executionOrder(AFFECTED)
                    .inMode(SELECTING)
            .enable();

        final Collection<TestResult> expectedTestResults = project
            .applyAsCommits("Single method body modification - sysout",
            "Inlined variable in a method");

        // when
        final TestResults actualTestResults = project
            .build()
                .options()
                    .withSystemProperties("scm.last.changes", "2")
                .configure()
            .run();

        // then
        softly.assertThat(actualTestResults.accumulatedPerTestClass()).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);

        // and also
        verifySmartTestingAPI(expectedTestResults, project);
    }

    private void verifySmartTestingAPI(Collection<TestResult> expectedTestResults, Project project) {
        // given
        Configuration configuration = Configuration.load();
        configuration.setStrategies("affected");
        configuration.setMode(RunMode.SELECTING);
        configuration.getScm().setLastChanges("2");

        final AffectedConfiguration affectedConfiguration = new AffectedConfiguration();
        affectedConfiguration.setTransitivity(true);

        configuration.setStrategiesConfiguration(Collections.singletonList(affectedConfiguration));

        configuration.dump(project.getRoot().toFile());
        List<String> expectedTestClasses = expectedTestResults
            .stream()
            .map(TestResult::getClassName)
            .collect(Collectors.toList());

        ArrayList<String> toOptimize = new ArrayList<>(expectedTestClasses);
        toOptimize.add(ProjectPersistTest.class.getName());
        toOptimize.add(ProjectPersistUsingPropertyTest.class.getName());
        toOptimize.add(HistoricalChangesNewTestsSelectionExecutionFunctionalTest.class.getName());

        // when
        Set<TestSelection> testSelections = SmartTesting
            .with(test -> test.endsWith("Test") || test.endsWith("TestCase"), configuration)
            .in(project.getRoot().toFile())
            .applyOnNames(toOptimize);

        Set<String> optimizedClassNames = SmartTesting.getNames(testSelections);

        // then
        softly.assertThat(optimizedClassNames).containsAll(expectedTestClasses).hasSameSizeAs(expectedTestClasses);
    }
}
