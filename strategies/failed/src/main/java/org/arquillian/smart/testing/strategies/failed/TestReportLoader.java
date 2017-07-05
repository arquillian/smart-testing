package org.arquillian.smart.testing.strategies.failed;

import java.util.Set;

public interface TestReportLoader {

    /**
     * Returns all test class names that contains failures.
     * @return Test classes with failing methods.
     */
    Set<String> loadTestResults();

}
