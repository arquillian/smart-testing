package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.Collection;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

class TestSelectorFromClasses extends TestSelector<Class<?>> {

    private final Iterable<Class<?>> testsToRun;

    TestSelectorFromClasses(TestExecutionPlannerLoader testExecutionPlannerLoader, File projectDir,
        Configuration configuration, Iterable<Class<?>> testsToRun) {
        super(configuration, testExecutionPlannerLoader, projectDir);
        this.testsToRun = testsToRun;
    }

    @Override
    public Collection<TestSelection> selectTestUsingPlanner(TestExecutionPlanner plannerForStrategy) {
        return plannerForStrategy.selectTestsFromClasses(testsToRun);
    }

    @Override
    protected Iterable<Class<?>> getTestsToRun() {
        return testsToRun;
    }

    @Override
    protected TestSelection createTestSelection(Class<?> testClass) {
        return new TestSelection(testClass.getName());
    }
}
