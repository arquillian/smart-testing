package org.arquillian.smart.testing.surefire.provider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

class TestStrategyApplier {

    static final String USAGE = "usage";

    private final TestExecutionPlannerLoader testExecutionPlannerLoader;
    private TestsToRun testsToRun;
    private ProviderParametersParser paramsProvider;

    TestStrategyApplier(TestsToRun testsToRun, ProviderParametersParser paramsProvider, TestExecutionPlannerLoader testExecutionPlannerLoader) {
        this.testsToRun = testsToRun;
        this.testExecutionPlannerLoader = testExecutionPlannerLoader;
        this.paramsProvider = paramsProvider;
    }

    TestsToRun apply(List<String> strategies) {
        final Set<Class<?>> smartTests = getTestsByRunningStrategies(strategies);

        if (isUsageSet() && isSelectingMode()) {
            return new TestsToRun(smartTests);
        } else {
            final Set<Class<?>> orderedTests = new LinkedHashSet<>(smartTests);
            testsToRun.iterator().forEachRemaining(orderedTests::add);

            return new TestsToRun(orderedTests);
        }

    }

    private boolean isSelectingMode() {
        return RunMode.SELECTING.name().equalsIgnoreCase(paramsProvider.getProperty(USAGE));
    }

    private boolean isUsageSet() {
        return paramsProvider.containsProperty(USAGE);
    }

    private Set<Class<?>> getTestsByRunningStrategies(List<String> strategies) {
        final Set<Class<?>> orderedTests = new LinkedHashSet<>();
        for (final String strategy : strategies) {

            final TestExecutionPlanner plannerForStrategy = testExecutionPlannerLoader.getPlannerForStrategy(strategy);
            final List<? extends Class<?>> tests = plannerForStrategy.getTests().stream().map(testClass -> {
                try {
                    return Class.forName(testClass);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Failed while obtaining strategy for " + strategy, e);
                }
            }).collect(Collectors.toList());
            orderedTests.addAll(tests);
        }
        return orderedTests;
    }
}
