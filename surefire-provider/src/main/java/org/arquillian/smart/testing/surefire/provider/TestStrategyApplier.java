package org.arquillian.smart.testing.surefire.provider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

class TestStrategyApplier {

    static final String FILTERING = "filtering";

    private final TestExecutionPlannerLoader testExecutionPlannerLoader;
    private TestsToRun testsToRun;
    private ProviderParametersParser paramsProvider;

    TestStrategyApplier(TestsToRun testsToRun, ProviderParametersParser paramsProvider, TestExecutionPlannerLoader testExecutionPlannerLoader) {
        this.testsToRun = testsToRun;
        this.testExecutionPlannerLoader = testExecutionPlannerLoader;
        this.paramsProvider = paramsProvider;
    }

    TestsToRun apply(List<String> orderStrategy) {
        final Set<Class<?>> smartTests = getTestsByRunningStrategies(orderStrategy);

        if (isFilteringMode()) {
            return new TestsToRun(smartTests);
        } else {
            final Set<Class<?>> orderedTests = new LinkedHashSet<>(smartTests);
            testsToRun.iterator().forEachRemaining(orderedTests::add);

            return new TestsToRun(orderedTests);
        }

    }

    private boolean isFilteringMode() {
        return paramsProvider.containsProperty(FILTERING);
    }

    private Set<Class<?>> getTestsByRunningStrategies(List<String> orderStrategy) {
        final Set<Class<?>> orderedTests = new LinkedHashSet<>();
        for (final String strategy : orderStrategy) {

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
