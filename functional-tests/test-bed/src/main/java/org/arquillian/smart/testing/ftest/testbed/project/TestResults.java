package org.arquillian.smart.testing.ftest.testbed.project;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.testresults.Status;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;

/**
 * Aggregates test results from the build execution and exposes them either on a test method granularity
 * or overall test class. In the latter case status is set to failing if any of the partial results is failing {@link
 * TestResult#isFailing()}.
 */
public class TestResults {

    private final List<TestResult> testResults;

    public TestResults(List<TestResult> testResults) {
        this.testResults = testResults;
    }

    public List<TestResult> accumulatedPerTestClass() {
        final Map<String, TestResult> testResultPerClass = new LinkedHashMap<>();
        testResults.forEach(methodTestResult -> {
            testResultPerClass.putIfAbsent(methodTestResult.getClassName(),
                new TestResult(methodTestResult.getClassName(), "*", methodTestResult.getStatus()));
            final TestResult perClassTestResult = testResultPerClass.get(methodTestResult.getClassName());
            if (methodTestResult.isFailing()) {
                testResultPerClass.put(methodTestResult.getClassName(),
                    new TestResult(methodTestResult.getClassName(), "*", Status.FAILURE));
            } else if (methodTestResult.isPassing() && !perClassTestResult.isFailing()) {
                // We might have SKIPPED methods stored so far
                // On the class level anything which is not failing we consider a success (even skipped)
                testResultPerClass.put(methodTestResult.getClassName(),
                    new TestResult(methodTestResult.getClassName(), "*", Status.PASSED));
            }
        });
        return Lists.newArrayList(testResultPerClass.values());
    }

    public List<TestResult> testsWithStatuses(Status... statuses) {
        final List<Status> expectedStatuses = Arrays.asList(statuses);

        if (expectedStatuses.isEmpty()) {
            return testResults;
        }

        return testResults.stream()
            .filter(testResult -> expectedStatuses.contains(testResult.getStatus()))
            .collect(Collectors.toList());
    }
}
