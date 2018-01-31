package org.arquillian.smart.testing.strategies.failed;

import java.util.Set;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestResult;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InProjectTestReportLoaderTest {

    @Test
    public void should_return_failing_test_classes() {
        // given
        final InProjectTestReportLoader surefireInProjectTestReportLoader =
            new InProjectTestReportLoader(new JavaSPILoader(), "src/test/resources");

        // when
        final Set<TestResult> testClassesWithFailingCases = surefireInProjectTestReportLoader.loadTestResults();

        // then
        assertThat(testClassesWithFailingCases)
            .containsExactlyInAnyOrder(
                new TestResult("org.arquillian.smart.testing.strategies.affected.ClassDependenciesGraphTest",
                "should_detect_simple_test_to_execute", TestResult.Result.FAILURE),
                new TestResult("org.arquillian.smart.testing.strategies.affected.ClassDependenciesGraphTest",
                    "should_detect_test_with_multiple_main_classes", TestResult.Result.FAILURE));

    }

}
