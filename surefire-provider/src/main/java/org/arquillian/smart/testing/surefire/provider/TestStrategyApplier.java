package org.arquillian.smart.testing.surefire.provider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

class TestStrategyApplier {

    private final TestExecutionPlannerLoader testExecutionPlannerLoader;
    private TestsToRun testsToRun;

    TestStrategyApplier(TestsToRun testsToRun, TestExecutionPlannerLoader testExecutionPlannerLoader) {
        this.testsToRun = testsToRun;
        this.testExecutionPlannerLoader = testExecutionPlannerLoader;
    }

    TestsToRun apply(List<String> orderStrategy) {
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
        testsToRun = new TestsToRun(orderedTests);
        return testsToRun;
    }

}
