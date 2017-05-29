package org.arquillian.smart.testing.surefire.provider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

import static java.lang.String.format;

class TestStrategyApplier {

    private static final Logger logger = Logger.getLogger(TestStrategyApplier.class.getName());


    static final String USAGE = "usage";

    private final TestExecutionPlannerLoader testExecutionPlannerLoader;
    private TestsToRun testsToRun;
    private ProviderParametersParser paramsProvider;
    private final ClassLoader testClassLoader;

    TestStrategyApplier(TestsToRun testsToRun, ProviderParametersParser paramsProvider,
        TestExecutionPlannerLoader testExecutionPlannerLoader, ProviderParameters bootParams) {
        this.testsToRun = testsToRun;
        this.testExecutionPlannerLoader = testExecutionPlannerLoader;
        this.paramsProvider = paramsProvider;
        this.testClassLoader = bootParams.getTestClassLoader();
    }

    TestsToRun apply(List<String> strategies) {
        final Set<Class<?>> selectedTests = selectTests(strategies);

        if (isUsageSet() && isSelectingMode()) {
            return new TestsToRun(selectedTests);
        } else {
            final Set<Class<?>> orderedTests = new LinkedHashSet<>(selectedTests);
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

    private Set<Class<?>> selectTests(List<String> strategies) {
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
        logger.log(Level.INFO, format("Applied strategies: %s", strategies));
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
