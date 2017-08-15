package org.arquillian.smart.testing.surefire.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

class TestStrategyApplier {

    private static final Logger logger = Logger.getLogger(TestStrategyApplier.class);
    private final TestExecutionPlannerLoader testExecutionPlannerLoader;
    private final ClassLoader testClassLoader;
    private TestsToRun testsToRun;

    TestStrategyApplier(TestsToRun testsToRun, TestExecutionPlannerLoader testExecutionPlannerLoader, ClassLoader testClassLoader) {
        this.testsToRun = testsToRun;
        this.testExecutionPlannerLoader = testExecutionPlannerLoader;
        this.testClassLoader = testClassLoader;
    }

    TestsToRun apply(Configuration configuration) {
        final Set<Class<?>> selectedTests = selectTests(configuration);

        if (testSelectionWithAnyStrategyIsChosen(configuration)) {
            return new TestsToRun(selectedTests);
        } else {
            final Set<Class<?>> orderedTests = new LinkedHashSet<>(selectedTests);
            testsToRun.iterator().forEachRemaining(orderedTests::add);
            return new TestsToRun(orderedTests);
        }
    }

    private Set<Class<?>> selectTests(Configuration configuration) {
        final List<String> strategies = Arrays.asList(configuration.getStrategies());
        if (strategies.isEmpty()) {
            logger.warn("Smart Testing Extension is installed but no strategies are provided. It won't influence the way how your tests are executed. "
                + "For details on how to configure it head over to http://bit.ly/st-config");
            return Collections.emptySet();
        }
        final Set<TestSelection> selectedTests = new LinkedHashSet<>();
        for (final String strategy : strategies) {
            final TestExecutionPlanner plannerForStrategy = testExecutionPlannerLoader.getPlannerForStrategy(strategy);
            selectedTests.addAll(plannerForStrategy.getTests());
        }
        logger.info("Applied strategies: %s", strategies);
        logger.info("Applied usage: [%s]", configuration.getMode().getName());

        return selectedTests
            .stream()
            .map(TestSelection::getClassName)
            .filter(this::presentOnClasspath)
            .map(testClass -> {
                try {
                    return Class.forName(testClass);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean testSelectionWithAnyStrategyIsChosen(Configuration configuration) {
        return configuration.isSelectingMode() && configuration.getStrategies().length > 0;
    }

    private boolean presentOnClasspath(String testClass) {
        try {
            Class<?> aClass = testClassLoader.loadClass(testClass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
