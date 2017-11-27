package org.arquillian.smart.testing.strategies.failed;
//tag::documentation[]
import java.io.File;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.StrategyConfiguration;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

import static org.arquillian.smart.testing.strategies.failed.FailedTestsDetector.FAILED;

public class FailedTestsDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public String alias() {
        return FAILED;
    } // <1>

    @Override
    public boolean isFor(String name) {
        return alias().equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir, TestVerifier verifier, Configuration configuration) {
        return new FailedTestsDetector(projectDir);
    } // <2>

    @Override
    public StrategyConfiguration strategyConfiguration() {
        return new FailedConfiguration();
    }
}
//end::documentation[]
