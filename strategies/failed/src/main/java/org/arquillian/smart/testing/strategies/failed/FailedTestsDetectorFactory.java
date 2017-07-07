package org.arquillian.smart.testing.strategies.failed;

import java.io.File;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

public class FailedTestsDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public String alias() {
        return "failed";
    }

    @Override
    public boolean isFor(String name) {
        return alias().equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir) {
        return new FailedTestsDetector();
    }

}
