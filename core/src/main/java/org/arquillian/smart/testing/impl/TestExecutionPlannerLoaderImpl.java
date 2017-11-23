package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

class TestExecutionPlannerLoaderImpl implements TestExecutionPlannerLoader {

    private final Map<String, TestExecutionPlannerFactory> availableStrategies = new HashMap<>();
    private final Configuration configuration;
    private final JavaSPILoader spiLoader;
    private final TestVerifier verifier;
    private final File projectDir;

    TestExecutionPlannerLoaderImpl(JavaSPILoader spiLoader, TestVerifier verifier, File projectDir,
        Configuration configuration) {
        this.configuration = configuration;
        this.spiLoader = spiLoader;
        this.verifier = verifier;
        this.projectDir = projectDir;
    }

    public TestExecutionPlanner getPlannerForStrategy(String strategy) {
        loadStrategies();

        if (availableStrategies.containsKey(strategy)) {
            return availableStrategies.get(strategy).create(projectDir, verifier, configuration);
        } else {
            throw new IllegalArgumentException(
                "No strategy found for [" + strategy + "]. Available strategies are: [" + availableStrategies.keySet()
                    + "]. Please make sure you have corresponding dependency defined.");
        }
    }

    private void loadStrategies() {
        if (!availableStrategies.isEmpty()) {
            return;
        }
        final Iterable<TestExecutionPlannerFactory> loadedStrategies = spiLoader.all(TestExecutionPlannerFactory.class);
        for (final TestExecutionPlannerFactory testExecutionPlannerFactory : loadedStrategies) {
            availableStrategies.put(testExecutionPlannerFactory.alias(), testExecutionPlannerFactory);
        }

        if (availableStrategies.isEmpty()) {
            throw new IllegalStateException("There is no strategy available. Please make sure you have corresponding "
                + "dependencies defined.");
        }
    }

    public TestVerifier getVerifier() {
        return verifier;
    }

    public Set<String> getAvailableStrategyNames() {
        loadStrategies();
        return availableStrategies.keySet();
    }
}
