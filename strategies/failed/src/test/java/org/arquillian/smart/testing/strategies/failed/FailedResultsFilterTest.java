package org.arquillian.smart.testing.strategies.failed;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.spi.TestResult;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import static org.arquillian.smart.testing.custom.assertions.TestSelectionCollectionAssert.assertThat;
import static org.arquillian.smart.testing.strategies.failed.FailedTestsDetector.FAILED;

public class FailedResultsFilterTest {

    private FailedConfiguration config;

    @Before
    public void prepareConfig() {
        config = (FailedConfiguration) ConfigurationLoader.load(new File("")).getStrategyConfiguration(FAILED);
    }

    @Test
    public void should_return_selections_with_failed_method() {
        // given
        Set<TestResult> results = new HashSet<>();
        results.add(new TestResult("FirstTestClass", "failedMethod", TestResult.Result.FAILURE));
        results.add(new TestResult("FirstTestClass", "passedMethod", TestResult.Result.PASSED));
        results.add(new TestResult("SecondTestClass", "failedMethod", TestResult.Result.FAILURE));
        results.add(new TestResult("SecondTestClass", "passedMethod", TestResult.Result.PASSED));

        // when
        Collection<TestSelection> failedTests = TestResultsFilter.getFailedTests(config, results);

        // then
        assertThat(failedTests).containsTestClassSelectionsExactlyInAnyOrder(
            new TestSelection("FirstTestClass", Arrays.asList("failedMethod"), FAILED),
            new TestSelection("SecondTestClass", Arrays.asList("failedMethod"), FAILED));
    }

    @Test
    public void should_not_return_any_selection_as_all_are_passed() {
        // given
        Set<TestResult> results = new HashSet<>();
        results.add(new TestResult("TestClass", "firstPassedMethod", TestResult.Result.PASSED));
        results.add(new TestResult("TestClass", "secondPassedMethod", TestResult.Result.PASSED));

        // when
        Collection<TestSelection> failedTests = TestResultsFilter.getFailedTests(config, results);

        // then
        Assertions.assertThat(failedTests).isEmpty();
    }

    @Test
    public void should_merge_selections_for_multiple_failed_methods_within_one_class() {
        // given
        Set<TestResult> results = new HashSet<>();
        results.add(new TestResult("FirstTestClass", "firstFailedMethod", TestResult.Result.FAILURE));
        results.add(new TestResult("FirstTestClass", "secondFailedMethod", TestResult.Result.FAILURE));
        results.add(new TestResult("FirstTestClass", "thirdFailedMethod", TestResult.Result.FAILURE));
        results.add(new TestResult("FirstTestClass", "passedMethod", TestResult.Result.PASSED));
        results.add(new TestResult("SecondTestClass", "failedMethod", TestResult.Result.FAILURE));
        results.add(new TestResult("SecondTestClass", "passedMethod", TestResult.Result.PASSED));

        // when
        Collection<TestSelection> failedTests = TestResultsFilter.getFailedTests(config, results);

        // then
        assertThat(failedTests).containsTestClassSelectionsExactlyInAnyOrder(
            new TestSelection("FirstTestClass",
                Arrays.asList("firstFailedMethod", "secondFailedMethod", "thirdFailedMethod"), FAILED),
            new TestSelection("SecondTestClass", Arrays.asList("failedMethod"), FAILED));
    }

    @Test
    public void should_return_selections_without_methods_specified_when_methods_resolution_in_config_is_disabled() {
        // given
        Set<TestResult> results = new HashSet<>();
        results.add(new TestResult("FirstTestClass", "firstFailedMethod", TestResult.Result.FAILURE));
        results.add(new TestResult("FirstTestClass", "secondFailedMethod", TestResult.Result.FAILURE));
        config.setMethods(false);

        // when
        Collection<TestSelection> failedTests = TestResultsFilter.getFailedTests(config, results);

        // then
        assertThat(failedTests).containsTestClassSelectionsExactlyInAnyOrder(new TestSelection("FirstTestClass", FAILED));
    }
}
