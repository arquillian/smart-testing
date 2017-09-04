package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.report.SmartTestingReportGenerator;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

import static java.lang.System.getProperty;

class TestStrategyApplier {

    private static final Logger logger = Logger.getLogger(TestStrategyApplier.class);
    private final TestExecutionPlannerLoader testExecutionPlannerLoader;
    private final ClassLoader testClassLoader;
    private final String baseDir;
    private TestsToRun testsToRun;

    TestStrategyApplier(TestsToRun testsToRun, TestExecutionPlannerLoader testExecutionPlannerLoader, ClassLoader testClassLoader, String baseDir) {
        this.testsToRun = testsToRun;
        this.testExecutionPlannerLoader = testExecutionPlannerLoader;
        this.testClassLoader = testClassLoader;
        this.baseDir = baseDir;
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
            return Collections.emptySet();
        }

        final List<TestSelection> selectedTests = new ArrayList<>();
        for (final String strategy : strategies) {
            final TestExecutionPlanner plannerForStrategy = testExecutionPlannerLoader.getPlannerForStrategy(strategy);
            selectedTests.addAll(plannerForStrategy.getTests());
        }
        logger.info("Applied strategies: %s", strategies);
        logger.info("Applied usage: [%s]", configuration.getMode().getName());

        final Collection<TestSelection> testSelections = filterMergeAndOrderTestSelection(selectedTests, strategies);

        if (isReportEnabled()) {
            final SmartTestingReportGenerator
                reportGenerator = new SmartTestingReportGenerator(testSelections, configuration, baseDir);
            reportGenerator.generateReport();
        }

        return testSelections
            .stream()
            .map(TestSelection::getClassName)
            .map(this::mapToClassInstance)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean testSelectionWithAnyStrategyIsChosen(Configuration configuration) {
        return configuration.isSelectingMode() && configuration.getStrategies().length > 0;
    }

    private boolean isInTestToRun(String testClass) {
        return testsToRun.getClassByName(testClass) != null;
    }

    private boolean presentOnClasspath(String testClass) {
        try {
            Class<?> aClass = testClassLoader.loadClass(testClass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private Class<?> mapToClassInstance(String testClass) {
        try {
            return Class.forName(testClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    Collection<TestSelection> filterMergeAndOrderTestSelection(Collection<TestSelection> selectedTests,
        List<String> strategies) {

        final Collection<TestSelection> testSelections = selectedTests
            .stream()
            .filter(testSelection -> presentOnClasspath(testSelection.getClassName()) && isInTestToRun(testSelection.getClassName()))
            .collect(Collectors.toMap(TestSelection::getClassName, Function.identity(), TestSelection::merge, LinkedHashMap::new))
            .values();

        if (strategies.size() > 1) {
            final StrategiesComparator byStrategies = new StrategiesComparator(strategies);

            return testSelections.stream()
                .sorted(Comparator.comparing(TestSelection::getTypes, byStrategies))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        return testSelections;
    }

    private boolean isReportEnabled() {
        return Boolean.valueOf(getProperty("smart.testing.report.enable", Boolean.toString(false)));
    }
}
