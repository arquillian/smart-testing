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
 * or overall test class. In the latter case status is set to failing if any of the partial results is failing {@link TestResult#isFailing()}.
 */
public class TestResults {

    private final List<TestResult> testResults;

    public TestResults(List<TestResult> testResults) {
        this.testResults = testResults;
    }

    public List<TestResult> accumulatedPerTestClass() {
        final Map<String, TestResult> testResultPerClass = new LinkedHashMap<>();
        testResults.forEach(methodTestResult -> {
            final TestResult perClassTestResult = new TestResult(methodTestResult.getClassName(), "*", methodTestResult.getStatus());
            testResultPerClass.putIfAbsent(methodTestResult.getClassName(), perClassTestResult);
            if (methodTestResult.isFailing()) {
                testResultPerClass.put(methodTestResult.getClassName(), new TestResult(methodTestResult.getClassName(), "*", Status.FAILURE));
            }
        });
        return Lists.newArrayList(testResultPerClass.values());
    }

    public List<TestResult> getTestsWithStatus(Status ... statuses) {
        final List<Status> expectedStatuses = Arrays.asList(statuses);
        return testResults.stream()
            .filter(testResult -> expectedStatuses.isEmpty() || expectedStatuses.contains(testResult.getStatus()))
            .collect(Collectors.toList());
    }

}
