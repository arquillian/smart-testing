package org.arquillian.smart.testing.strategies.categorized;

import java.io.File;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.StrategyConfiguration;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

import static org.arquillian.smart.testing.strategies.categorized.CategorizedTestsDetector.CATEGORIZED;

public class CategorizedTestsDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public String alias() {
        return CATEGORIZED;
    }

    @Override
    public boolean isFor(String name) {
        return alias().equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir, TestVerifier verifier, Configuration configuration) {
        return new CategorizedTestsDetector(configuration);
    }

    @Override
    public StrategyConfiguration strategyConfiguration() {
        return new CategorizedConfiguration();
    }
}
