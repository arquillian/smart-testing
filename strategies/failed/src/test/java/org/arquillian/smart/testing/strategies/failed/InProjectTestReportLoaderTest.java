package org.arquillian.smart.testing.strategies.failed;

import java.util.Set;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InProjectTestReportLoaderTest {

    @Test
    public void should_return_failing_test_classes() {
        // given
        final InProjectTestReportLoader surefireInProjectTestReportLoader =
            new InProjectTestReportLoader(new JavaSPILoader(), "src/test/resources");

        // when
        final Set<String> testClassesWithFailingCases = surefireInProjectTestReportLoader.loadTestResults();

        // then
        assertThat(testClassesWithFailingCases)
            .containsExactly("org.arquillian.smart.testing.strategies.affected.ClassDependenciesGraphTest");

    }

}
