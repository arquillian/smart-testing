package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.Collection;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

class TestSelectorFromNames extends TestSelector<String> {

    private final Iterable<String> testsToRun;

    TestSelectorFromNames(TestExecutionPlannerLoader testExecutionPlannerLoader, File projectDir,
        Configuration configuration, Iterable<String> testsToRun) {
        super(configuration, testExecutionPlannerLoader, projectDir);
        this.testsToRun = testsToRun;
    }

    @Override
    public Collection<TestSelection> selectTestUsingPlanner(TestExecutionPlanner plannerForStrategy) {
        return plannerForStrategy.selectTestsFromNames(testsToRun);
    }

    @Override
    protected Iterable<String> getTestsToRun() {
        return testsToRun;
    }

    @Override
    protected TestSelection createTestSelection(String testClass) {
        return new TestSelection(testClass);
    }
}
