package org.arquillian.smart.testing.ftest.simple;

import java.util.List;
import org.arquillian.smart.testing.ftest.TestBedTemplate;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DisabledSmartTestingTestSelection extends TestBedTemplate {

    @Test
    public void should_execute_all_tests_when_disable_smart_testing_is_disabled() throws Exception {
        // when
        final List<TestResult> actualTestResults = project
            .buildOptions()
                .withSystemProperties("disableSmartTesting", "true")
                .configure()
            .build();

        // then
        assertThat(actualTestResults).hasSize(77);
    }
}
