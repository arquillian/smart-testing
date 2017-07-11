package org.arquillian.smart.testing.surefire.provider;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

class TestStrategyApplier {

    static final String USAGE = "usage";
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

        if (isUsageSet(configuration) && isSelectingMode(configuration)) {
            return new TestsToRun(selectedTests);
        } else {
            final Set<Class<?>> orderedTests = new LinkedHashSet<>(selectedTests);
            testsToRun.iterator().forEachRemaining(orderedTests::add);
            return new TestsToRun(orderedTests);
        }
    }

    private boolean isSelectingMode(Configuration configuration) {
        return RunMode.SELECTING.name().equalsIgnoreCase(configuration.getMode());
    }

    private boolean isUsageSet(Configuration configuration) {
        return configuration.isModeSet();
    }

    private Set<Class<?>> selectTests(Configuration configuration) {
        final List<String> strategies = Arrays.asList(configuration.getStrategies());
        final Set<Class<?>> orderedTests = new LinkedHashSet<>();
        for (final String strategy : strategies) {

            final TestExecutionPlanner plannerForStrategy = testExecutionPlannerLoader.getPlannerForStrategy(strategy);
            final List<? extends Class<?>> tests = plannerForStrategy.getTests()
                .stream()
                .filter(this::presentOnClasspath)
                .filter(this::isInTestToRun)
                .map(testClass -> {
                    try {
                        return Class.forName(testClass);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("Failed while obtaining strategy for " + strategy, e);
                    }
                }).collect(Collectors.toList());
            orderedTests.addAll(tests);
        }
        logger.info("Applied strategies: %s", strategies);
        logger.info("Applied usage: [%s]", isSelectingMode(configuration) ? "selecting" : "ordering");
        return orderedTests;
    }

    private boolean presentOnClasspath(String testClass) {
        try {
            Class<?> aClass = testClassLoader.loadClass(testClass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isInTestToRun(String testClass) {
        return testsToRun.getClassByName(testClass) != null;
    }
}
