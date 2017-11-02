package org.arquillian.smart.testing.spi;

import java.io.File;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;

// TODO remove this extra layer in favor of SPI/DI on concrete planners
public interface TestExecutionPlannerFactory {

    String alias();

    boolean isFor(String name);

    TestExecutionPlanner create(File projectDir, TestVerifier testVerifier, Configuration configuration);

    StrategyConfiguration strategyConfiguration();

}
