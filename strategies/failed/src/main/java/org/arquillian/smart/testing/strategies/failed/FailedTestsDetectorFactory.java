package org.arquillian.smart.testing.strategies.failed;

import java.io.File;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

public class FailedTestsDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public boolean isFor(String name) {
        return "failed".equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir, String[] globPatterns) {
        return new FailedTestsDetector();
    }

}
