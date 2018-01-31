package org.arquillian.smart.testing.strategies.failed;

import java.util.Set;
import org.arquillian.smart.testing.spi.TestResult;

public interface TestReportLoader {

    /**
     * Returns all test results
     * @return Loaded test results
     */
    Set<TestResult> loadTestResults();

}
