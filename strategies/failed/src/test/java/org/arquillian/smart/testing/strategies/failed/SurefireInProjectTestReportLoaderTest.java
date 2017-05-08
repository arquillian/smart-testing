package org.arquillian.smart.testing.strategies.failed;

import java.util.Set;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SurefireInProjectTestReportLoaderTest {


    @Test
    public void should_return_failing_test_classes() {

        // given

        final SurefireInProjectTestReporterLoader surefireInProjectTestReporterLoader = new SurefireInProjectTestReporterLoader();
        surefireInProjectTestReporterLoader.setInProjectDir("src/test/resources");

        // when

        final Set<String> testClassesWithFailingCases = surefireInProjectTestReporterLoader.loadTestResults();

        // then
        assertThat(testClassesWithFailingCases)
            .containsExactly("org.arquillian.smart.testing.strategies.affected.ClassFileIndexTest");

    }

}
