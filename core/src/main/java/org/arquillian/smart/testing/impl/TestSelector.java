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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.report.SmartTestingReportGenerator;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

abstract class TestSelector<CLASS_INFO_TYPE> {

    private static final Logger logger = Log.getLogger();
    private final TestExecutionPlannerLoader testExecutionPlannerLoader;
    private final File projectDir;
    private final Configuration configuration;

    TestSelector(Configuration configuration, TestExecutionPlannerLoader testExecutionPlannerLoader, File projectDir) {
        this.configuration = configuration;
        this.testExecutionPlannerLoader = testExecutionPlannerLoader;
        this.projectDir = projectDir;
    }

    protected abstract Collection<TestSelection> selectTestUsingPlanner(TestExecutionPlanner plannerForStrategy);

    protected abstract Iterable<CLASS_INFO_TYPE> getTestsToRun();

    protected abstract TestSelection createTestSelection(CLASS_INFO_TYPE testClass);

    Set<TestSelection> orderTests() {
        final Set<TestSelection> orderedTests = new LinkedHashSet<>(selectTests());
        getTestsToRun()
            .iterator()
            .forEachRemaining(testClass -> orderedTests.add(createTestSelection(testClass)));
        return orderedTests;
    }

    Set<TestSelection> selectTests() {
        List<String> strategies = retrieveStrategies();
        if (strategies.isEmpty()) {
            logger.warn(
                "Smart Testing Extension is installed but no strategies are provided. It won't influence the way how your tests are executed. "
                    + "For details on how to configure it head over to http://bit.ly/st-config");
            return Collections.emptySet();
        }

        final List<TestSelection> selectedTests = new ArrayList<>();
        for (final String strategy : strategies) {
            final TestExecutionPlanner plannerForStrategy = testExecutionPlannerLoader.getPlannerForStrategy(strategy);
            selectedTests.addAll(selectTestUsingPlanner(plannerForStrategy));
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
            new SmartTestingReportGenerator(testSelections, configuration, projectDir).generateReport();
        }

        return new LinkedHashSet<>(testSelections);
    }

    private List<String> retrieveStrategies() {
        if (configuration.getStrategies().length == 0){
            return Collections.emptyList();
        }
        List<String> errorMessages = new ArrayList<>();
        configuration.autocorrectStrategies(testExecutionPlannerLoader.getAvailableStrategyNames(), errorMessages);
        errorMessages.forEach(msg -> logger.error(msg));

        return Arrays.asList(configuration.getStrategies());
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
