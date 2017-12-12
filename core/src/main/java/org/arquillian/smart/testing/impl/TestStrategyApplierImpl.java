package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.Set;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.TestStrategyApplier;
import org.arquillian.smart.testing.configuration.Configuration;

class TestStrategyApplierImpl implements TestStrategyApplier {

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
        TestSelectorFromNames testSelector =
            new TestSelectorFromNames(testExecutionPlannerLoader, projectDir, configuration, testsToRun);
        return apply(testSelector);
    }


    public Set<TestSelection> applyOnClasses(Iterable<Class<?>> testsToRun) {
        TestSelectorFromClasses testSelector =
            new TestSelectorFromClasses(testExecutionPlannerLoader, projectDir, configuration, testsToRun);
        return apply(testSelector);
    }

    private Set<TestSelection> apply(TestSelector testSelector) {

        if (testSelectionWithAnyStrategyIsChosen(configuration)) {
            return testSelector.selectTests();
        } else {
            return testSelector.orderTests();
        }
    }

    private boolean testSelectionWithAnyStrategyIsChosen(Configuration configuration) {
        return configuration.isSelectingMode() && configuration.getStrategies().length > 0;
    }
}
