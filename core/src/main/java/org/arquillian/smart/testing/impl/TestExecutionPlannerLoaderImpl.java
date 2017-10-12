package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.StringSimilarityCalculator;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

class TestExecutionPlannerLoaderImpl implements TestExecutionPlannerLoader {

    private final Map<String, TestExecutionPlannerFactory> availableStrategies = new HashMap<>();
    private final JavaSPILoader spiLoader;
    private final TestVerifier verifier;
    private final File projectDir;

    TestExecutionPlannerLoaderImpl(JavaSPILoader spiLoader, TestVerifier verifier, File projectDir) {
        this.spiLoader = spiLoader;
        this.verifier = verifier;
        this.projectDir = projectDir;
    }

    public TestExecutionPlanner getPlannerForStrategy(String strategy, boolean autocorrect) {

        if (availableStrategies.isEmpty()) {
            loadStrategies();
        }

        if (availableStrategies.containsKey(strategy)) {
            return availableStrategies.get(strategy).create(projectDir, verifier);
        } else {
            if (autocorrect) {
                final StringSimilarityCalculator stringSimilarityCalculator = new StringSimilarityCalculator();
                final String closestMatch =
                    stringSimilarityCalculator.findClosestMatch(strategy, availableStrategies.keySet());
                if (availableStrategies.containsKey(closestMatch)) {
                    return availableStrategies.get(closestMatch).create(projectDir, verifier);
                }
            }
        }

        throw new IllegalArgumentException("No strategy found for [" + strategy + "]. Available strategies are: [" + availableStrategies.keySet()
            + "]. Please make sure you have corresponding dependency defined.");
    }

    private void loadStrategies() {
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
}
