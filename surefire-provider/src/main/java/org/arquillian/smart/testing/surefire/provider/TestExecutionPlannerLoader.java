package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

class TestExecutionPlannerLoader {

    private final Map<String, TestExecutionPlannerFactory> availableStrategies = new HashMap<>();
    private final String[] globPatterns;

    TestExecutionPlannerLoader(String[] globPatterns) { // TODO refactor as inclusion/exclusion fix https://github.com/arquillian/smart-testing/issues/8
        this.globPatterns = globPatterns;
    }

    TestExecutionPlanner getPlannerForStrategy(String strategy) {

        if (availableStrategies.isEmpty()) {
            loadStrategies();
        }

        if (availableStrategies.containsKey(strategy)) {
            final File projectDir = new File(System.getProperty("user.dir"));
            return availableStrategies.get(strategy).create(projectDir, globPatterns);
        }

        throw new IllegalArgumentException("No strategy found for [" + availableStrategies.keySet()
            + "]. Please make sure you have corresponding dependency defined.");
    }

    private void loadStrategies() {
        final ServiceLoader<TestExecutionPlannerFactory> loadedStrategies = ServiceLoader.load(TestExecutionPlannerFactory.class);
        for (final TestExecutionPlannerFactory testExecutionPlannerFactory : loadedStrategies) {
            availableStrategies.put(testExecutionPlannerFactory.alias(), testExecutionPlannerFactory);
        }

        if (availableStrategies.isEmpty()) {
            throw new IllegalStateException("There is no strategy available. Please make sure you have corresponding "
                + "dependencies defined.");
        }
    }
}
