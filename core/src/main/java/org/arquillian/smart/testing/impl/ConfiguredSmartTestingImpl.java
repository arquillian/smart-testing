package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.Set;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.ConfiguredSmartTesting;
import org.arquillian.smart.testing.api.TestStrategyApplier;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.spi.JavaSPILoader;

public class ConfiguredSmartTestingImpl implements ConfiguredSmartTesting {

    private Configuration configuration;
    private TestExecutionPlannerLoader testExecutionPlannerLoader;
    private TestVerifier testVerifier;
    private File projectDir;

    public ConfiguredSmartTestingImpl(TestVerifier testVerifier) {
        this.testVerifier = testVerifier;
    }

    public ConfiguredSmartTestingImpl(TestVerifier testVerifier, Configuration configuration) {
        this.testVerifier = testVerifier;
        this.configuration = configuration;
    }

    ConfiguredSmartTestingImpl(TestExecutionPlannerLoader testExecutionPlannerLoader, Configuration configuration) {
        this.testExecutionPlannerLoader = testExecutionPlannerLoader;
        this.configuration = configuration;
    }

    @Override
    public TestStrategyApplier in(String projectDirectory) {
        projectDir = new File(projectDirectory);
        return createApplier();
    }

    @Override
    public TestStrategyApplier in(File projectDirectory) {
        projectDir = projectDirectory;
        return createApplier();
    }

    @Override
    public Set<TestSelection> applyOnNames(Iterable<String> testsToRun) {
        return createApplier().applyOnNames(testsToRun);
    }

    @Override
    public Set<TestSelection> applyOnClasses(Iterable<Class<?>> testsToRun) {
        return createApplier().applyOnClasses(testsToRun);
    }

    private TestStrategyApplier createApplier() {
        if (projectDir == null){
            String basedir = System.getProperty("basedir");
            projectDir = new File(basedir != null ? basedir : ".");
        }
        if (configuration == null) {
            configuration = ConfigurationLoader.loadPrecalculated(projectDir);
        }
        if (testExecutionPlannerLoader == null) {
            testExecutionPlannerLoader =
                new TestExecutionPlannerLoaderImpl(new JavaSPILoader(), testVerifier, projectDir, configuration);
        }
        return new TestStrategyApplierImpl(configuration, testExecutionPlannerLoader, projectDir);
    }
}
