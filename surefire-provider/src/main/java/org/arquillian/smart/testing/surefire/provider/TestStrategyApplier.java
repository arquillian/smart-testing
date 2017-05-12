package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

class TestStrategyApplier {

    private final ProviderParametersParser paramParser;
    private TestsToRun testsToRun;

    TestStrategyApplier(TestsToRun testsToRun, ProviderParametersParser paramParser) {
        this.testsToRun = testsToRun;
        this.paramParser = paramParser;
    }

    TestsToRun apply(List<String> orderStrategy) {
        final Set<Class<?>> orderedTests = new LinkedHashSet<>();
        for (final String strategy : orderStrategy) {

            final TestExecutionPlanner plannerForStrategy = getPlannerForStrategy(strategy);
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

    private TestExecutionPlanner getPlannerForStrategy(String orderStrategy) {
        final File projectDir = new File(System.getProperty("user.dir"));
        final String[] globPatternsAsArray = getGlobPatterns();

        for (final TestExecutionPlannerFactory testExecutionPlanner : ServiceLoader.load(TestExecutionPlannerFactory.class)) {
            if (testExecutionPlanner.isFor(orderStrategy)) {
                return testExecutionPlanner.create(projectDir, globPatternsAsArray);
            }
        }

        throw new IllegalArgumentException("No strategy found for [" + orderStrategy
            + "]. Please make sure you have corresponding dependency defined.");
    }

    private String[] getGlobPatterns() {
        final List<String> globPatterns = paramParser.getIncludes();
        // TODO question why exclusions are added too?
        globPatterns.addAll(paramParser.getExcludes());
        return globPatterns.toArray(new String[globPatterns.size()]);
    }
}
