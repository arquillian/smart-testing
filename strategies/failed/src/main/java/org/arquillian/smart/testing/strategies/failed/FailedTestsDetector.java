package org.arquillian.smart.testing.strategies.failed;

import java.util.Collection;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

public class FailedTestsDetector implements TestExecutionPlanner {

    private TestReportLoader testReportLoader = new InProjectTestReportLoader(new JavaSPILoader());

    @Override
    public Collection<String> getTests() {
        return testReportLoader.loadTestResults();
    }
}
