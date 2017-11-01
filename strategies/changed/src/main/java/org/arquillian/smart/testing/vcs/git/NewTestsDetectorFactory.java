package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.StrategyConfiguration;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

import static org.arquillian.smart.testing.vcs.git.NewTestsDetector.NEW;

public class NewTestsDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public String alias() {
        return NEW;
    }

    @Override
    public boolean isFor(String name) {
        return alias().equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir, TestVerifier verifier, Configuration configuration) {
        return new NewTestsDetector(projectDir, verifier, configuration);
    }

    @Override
    public StrategyConfiguration strategyConfiguration() {
        return new NewConfiguration();
    }
}
