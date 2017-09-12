package org.arquillian.smart.testing.ftest.configuration.assertions;

import java.util.Arrays;
import java.util.List;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;

import static org.assertj.core.api.Assertions.tuple;

public class TestResultAssert extends ListAssert<TestResult> {

    private TestResultAssert(List<TestResult> testResults) {
        super(testResults);
    }

    public static TestResultAssert assertThat(List<TestResult> testResults) {
        return new TestResultAssert(testResults);
    }

    // Verifying TestResult has same className & methodName, which indicates it's skipped by either
    // skipAfterFailureCount or `@Ignore` on class level.
    public TestResultAssert hasSkippedClasses(String... classes) {
         this.extracting(TestResult::getClassName, TestResult::getTestMethod)
            .contains(Arrays.stream(classes).map(className -> tuple(className, className)).toArray(Tuple[]::new));

         return this;
    }

    public TestResultAssert doesNotHaveSkippedClasses(String... classes) {
        this.extracting(TestResult::getClassName, TestResult::getTestMethod)
            .doesNotContain(Arrays.stream(classes).map(className -> tuple(className, className)).toArray(Tuple[]::new));

        return this;
    }
}
