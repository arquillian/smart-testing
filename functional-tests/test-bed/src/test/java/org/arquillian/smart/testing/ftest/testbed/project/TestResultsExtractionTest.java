package org.arquillian.smart.testing.ftest.testbed.project;

import java.util.List;
import org.arquillian.smart.testing.ftest.testbed.testresults.Status;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestResultsExtractionTest {

    @Test
    public void should_merge_all_test_methods_of_one_class_to_successful_for_only_successful_executions()
        throws Exception {
        // given
        final TestResult should_pass_1 =
            new TestResult("org.arquillian.test.Test", "should_pass_1", Status.PASSED);
        final TestResult should_pass_2 =
            new TestResult("org.arquillian.test.Test", "should_pass_2", Status.PASSED);

        final TestResults testResults = new TestResults(asList(should_pass_1, should_pass_2));

        // when
        final List<TestResult> accumulatedPerTestClass = testResults.accumulatedPerTestClass();

        // then
        assertThat(accumulatedPerTestClass).hasSize(1)
            .containsExactly(new TestResult("org.arquillian.test.Test", "*", Status.PASSED));
    }

    @Test
    public void should_merge_all_test_methods_of_one_class_to_successful_for_skipped_successful_executions()
        throws Exception {
        // given
        final TestResult should_pass_1 =
            new TestResult("org.arquillian.test.Test", "should_pass_1", Status.PASSED);
        final TestResult should_skip =
            new TestResult("org.arquillian.test.Test", "should_skip", Status.SKIPPED);

        final TestResults testResults = new TestResults(asList(should_pass_1, should_skip));

        // when
        final List<TestResult> accumulatedPerTestClass = testResults.accumulatedPerTestClass();

        // then
        assertThat(accumulatedPerTestClass).hasSize(1)
            .containsExactly(new TestResult("org.arquillian.test.Test", "*", Status.PASSED));
    }

    @Test
    public void should_merge_all_test_methods_of_one_class_to_skipped_for_skipped_executions()
        throws Exception {
        // given
        final TestResult should_skip_1 =
            new TestResult("org.arquillian.test.Test", "should_skip_1", Status.SKIPPED);
        final TestResult should_skip_2 =
            new TestResult("org.arquillian.test.Test", "should_skip_2", Status.SKIPPED);

        final TestResults testResults = new TestResults(asList(should_skip_1, should_skip_2));

        // when
        final List<TestResult> accumulatedPerTestClass = testResults.accumulatedPerTestClass();

        // then
        assertThat(accumulatedPerTestClass).hasSize(1)
            .containsExactly(new TestResult("org.arquillian.test.Test", "*", Status.SKIPPED));
    }

    @Test
    public void should_merge_all_test_methods_of_one_class_to_failed_for_failed_executions()
        throws Exception {
        // given
        final TestResult should_fail_1 =
            new TestResult("org.arquillian.test.Test", "should_fail_1", Status.FAILURE);
        final TestResult should_fail_2 =
            new TestResult("org.arquillian.test.Test", "should_fail_2", Status.FAILURE);

        final TestResults testResults = new TestResults(asList(should_fail_1, should_fail_2));

        // when
        final List<TestResult> accumulatedPerTestClass = testResults.accumulatedPerTestClass();

        // then
        assertThat(accumulatedPerTestClass).hasSize(1)
            .containsExactly(new TestResult("org.arquillian.test.Test", "*", Status.FAILURE));
    }

    @Test
    public void should_merge_all_test_methods_of_one_class_to_failed_for_failed_and_passed_executions()
        throws Exception {
        // given
        final TestResult should_fail =
            new TestResult("org.arquillian.test.Test", "should_fail", Status.FAILURE);
        final TestResult should_pass =
            new TestResult("org.arquillian.test.Test", "should_pass", Status.PASSED);

        final TestResults testResults = new TestResults(asList(should_fail, should_pass));

        // when
        final List<TestResult> accumulatedPerTestClass = testResults.accumulatedPerTestClass();

        // then
        assertThat(accumulatedPerTestClass).hasSize(1)
            .containsExactly(new TestResult("org.arquillian.test.Test", "*", Status.FAILURE));
    }

    @Test
    public void should_merge_all_test_methods_of_one_class_to_failed_for_failed_skipped_and_succesful_executions()
        throws Exception {
        // given
        final TestResult should_fail =
            new TestResult("org.arquillian.test.Test", "should_fail", Status.FAILURE);
        final TestResult should_pass =
            new TestResult("org.arquillian.test.Test", "should_pass", Status.PASSED);
        final TestResult should_skip =
            new TestResult("org.arquillian.test.Test", "should_skip", Status.SKIPPED);

        final TestResults testResults = new TestResults(asList(should_fail, should_pass, should_skip));

        // when
        final List<TestResult> accumulatedPerTestClass = testResults.accumulatedPerTestClass();

        // then
        assertThat(accumulatedPerTestClass).hasSize(1)
            .containsExactly(new TestResult("org.arquillian.test.Test", "*", Status.FAILURE));
    }

    @Test
    public void should_merge_all_test_methods_of_several_classes_with_respective_aggregated_statuses()
        throws Exception {
        // given
        final TestResult should_fail = new TestResult("org.arquillian.test.Test", "should_fail", Status.FAILURE);
        final TestResult should_skip = new TestResult("org.arquillian.test.Test", "should_skip", Status.SKIPPED);

        final TestResult should_pass_1 = new TestResult("org.arquillian.test.OtherTest", "should_pass_1", Status.PASSED);
        final TestResult should_pass_2 = new TestResult("org.arquillian.test.OtherTest", "should_pass_2", Status.PASSED);

        final TestResults testResults = new TestResults(asList(should_fail, should_skip, should_pass_1, should_pass_2));

        // when
        final List<TestResult> accumulatedPerTestClass = testResults.accumulatedPerTestClass();

        // then
        assertThat(accumulatedPerTestClass).hasSize(2)
            .containsExactly(new TestResult("org.arquillian.test.Test", "*", Status.FAILURE),
                new TestResult("org.arquillian.test.OtherTest", "*", Status.PASSED));
    }

}
