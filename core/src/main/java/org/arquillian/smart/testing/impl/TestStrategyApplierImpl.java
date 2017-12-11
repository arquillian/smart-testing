package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.TestStrategyApplier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.report.SmartTestingReportGenerator;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

class TestStrategyApplierImpl implements TestStrategyApplier {

    private static final Logger logger = Log.getLogger();
    private final TestExecutionPlannerLoader testExecutionPlannerLoader;
    private final File projectDir;
    private final Configuration configuration;

    TestStrategyApplierImpl(Configuration configuration, TestExecutionPlannerLoader testExecutionPlannerLoader,
        File projectDir) {
        this.configuration = configuration;
        this.testExecutionPlannerLoader = testExecutionPlannerLoader;
        this.projectDir = projectDir;
    }

    public Set<TestSelection> applyOnNames(Iterable<String> testsToRun) {
        return apply(testsToRun, className -> className, this::selectTestsFromNames);
    }

    private Collection<TestSelection> selectTestsFromNames(TestExecutionPlanner testExecutionPlanner,
        Iterable<String> testsToRun) {
        return testExecutionPlanner.selectTestsFromNames(testsToRun);
    }

    public Set<TestSelection> applyOnClasses(Iterable<Class<?>> testsToRun) {
        return apply(testsToRun, Class::getName, this::selectTestsFromClasses);
    }

    private Collection<TestSelection> selectTestsFromClasses(TestExecutionPlanner testExecutionPlanner,
        Iterable<Class<?>> testsToRun) {
        return testExecutionPlanner.selectTestsFromClasses(testsToRun);
    }

    private <TESTCLASS> Set<TestSelection> apply(Iterable<TESTCLASS> testsToRun,
        Function<TESTCLASS, String> mapperToName,
        BiFunction<TestExecutionPlanner, Iterable<TESTCLASS>, Collection<TestSelection>> selectionFunction) {

        final Set<TestSelection> selectedTests = selectTests(configuration, testsToRun, selectionFunction);
        if (testSelectionWithAnyStrategyIsChosen(configuration)) {
            return selectedTests;
        } else {
            final Set<TestSelection> orderedTests = new LinkedHashSet<>(selectedTests);
            testsToRun
                .iterator()
                .forEachRemaining(testclass -> orderedTests.add(new TestSelection(mapperToName.apply(testclass))));
            return orderedTests;
        }
    }

    private <TESTCLASS> Set<TestSelection> selectTests(Configuration configuration, Iterable<TESTCLASS> testsToRun,
        BiFunction<TestExecutionPlanner, Iterable<TESTCLASS>, Collection<TestSelection>> selectionFunction) {
        if (configuration.getStrategies().length == 0) {
            logger.warn(
                "Smart Testing Extension is installed but no strategies are provided. It won't influence the way how your tests are executed. "
                    + "For details on how to configure it head over to http://bit.ly/st-config");
            return Collections.emptySet();
        }

        List<String> errorMessages = new ArrayList<>();
        configuration.autocorrectStrategies(testExecutionPlannerLoader.getAvailableStrategyNames(), errorMessages);
        errorMessages.forEach(msg -> logger.error(msg));

        List<String> strategies = Arrays.asList(configuration.getStrategies());
        final List<TestSelection> selectedTests = new ArrayList<>();
        for (final String strategy : strategies) {
            final TestExecutionPlanner plannerForStrategy = testExecutionPlannerLoader.getPlannerForStrategy(strategy);
            selectedTests.addAll(selectionFunction.apply(plannerForStrategy, testsToRun));
        }
        logger.info("Applied strategies: %s", strategies);
        logger.info("Applied usage: [%s]", configuration.getMode().getName());
        final Collection<TestSelection> testSelections = filterMergeAndOrderTestSelection(selectedTests, strategies);

        if (testSelections.isEmpty()) {
            logger.debug("Applied test selections: %s", "No tests selected as per the strategy chosen.");
        } else {
            logger.debug("Applied test selections: %s", testSelections.toString());
        }

        if (isReportEnabled() || logger.isDebug()) {
            final SmartTestingReportGenerator
                reportGenerator = new SmartTestingReportGenerator(testSelections, configuration, projectDir);
            reportGenerator.generateReport();
        }

        return new LinkedHashSet<>(testSelections);
    }

    private boolean testSelectionWithAnyStrategyIsChosen(Configuration configuration) {
        return configuration.isSelectingMode() && configuration.getStrategies().length > 0;
    }

    Collection<TestSelection> filterMergeAndOrderTestSelection(Collection<TestSelection> selectedTests,
        List<String> strategies) {

        final Collection<TestSelection> testSelections = selectedTests
            .stream()
            .filter(testExecutionPlannerLoader.getVerifier()::isTest)
            .collect(Collectors.toMap(TestSelection::getClassName, Function.identity(), TestSelection::merge,
                LinkedHashMap::new))
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
        return configuration.getReport().isEnable();
    }
}
