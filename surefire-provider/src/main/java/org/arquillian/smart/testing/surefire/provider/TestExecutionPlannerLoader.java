package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

class TestExecutionPlannerLoader {

    private final Map<String, TestExecutionPlanner> availableStrategies = new HashMap<>();
    private final String[] globPatterns;

    TestExecutionPlannerLoader(String[] globPatterns) { // TODO refactor as inclusion/exclusion fix https://github.com/arquillian/smart-testing/issues/8
        this.globPatterns = globPatterns;
    }

    public TestExecutionPlanner getPlannerForStrategy(String strategy) {

        if (availableStrategies.isEmpty()) {
            loadStrategies();
        }

        if (availableStrategies.containsKey(strategy)) {
            return availableStrategies.get(strategy);
        }

        throw new IllegalArgumentException("No strategy found for [" + strategy
            + "]. Please make sure you have corresponding dependency defined.");
    }

    private void loadStrategies() {
        for (final TestExecutionPlannerFactory testExecutionPlannerFactory : ServiceLoader.load(TestExecutionPlannerFactory.class)) {
            final File projectDir = new File(System.getProperty("user.dir"));
            final TestExecutionPlanner testExecutionPlanner =
                testExecutionPlannerFactory.create(projectDir, this.globPatterns);
            availableStrategies.put(testExecutionPlannerFactory.alias(), testExecutionPlanner);
        }

    }
}
