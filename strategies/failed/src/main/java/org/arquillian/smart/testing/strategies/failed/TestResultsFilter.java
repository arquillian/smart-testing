package org.arquillian.smart.testing.strategies.failed;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.spi.TestResult;

public class TestResultsFilter {

    static Collection<TestSelection> getFailedTests(FailedConfiguration strategyConfig, Set<TestResult> testResults) {

        return testResults
            .stream()
            .filter(TestResult::isFailing)
            .map(testResult -> createTestSelection(strategyConfig, testResult))
            .collect(
                Collectors.toMap(TestSelection::getClassName, Function.identity(), TestSelection::merge,
                    LinkedHashMap::new))
            .values();
    }

    private static TestSelection createTestSelection(FailedConfiguration strategyConfig, TestResult testResult) {
        if (strategyConfig.isMethods()) {
            return new TestSelection(testResult.getClassName(), Arrays.asList(testResult.getTestMethod()),
                FailedTestsDetector.FAILED);
        } else {
            return new TestSelection(testResult.getClassName(), FailedTestsDetector.FAILED);
        }
    }
}
