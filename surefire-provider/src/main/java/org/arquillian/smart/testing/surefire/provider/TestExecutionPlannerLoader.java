package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.surefire.testset.TestRequest;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

class TestExecutionPlannerLoader {

    private final Map<String, TestExecutionPlannerFactory> availableStrategies = new HashMap<>();
    private final JavaSPILoader spiLoader;
    private final File testSourceDirectory;

    TestExecutionPlannerLoader(JavaSPILoader spiLoader, File testSourceDir) {
        this.spiLoader = spiLoader;
        this.testSourceDirectory = testSourceDir;
    }

    TestExecutionPlanner getPlannerForStrategy(String strategy) {

        if (availableStrategies.isEmpty()) {
            loadStrategies();
        }

        if (availableStrategies.containsKey(strategy)) {
            final File projectDir = new File(System.getProperty("user.dir"));
            return availableStrategies.get(strategy).create(projectDir, testSourceDirectory);
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
}
