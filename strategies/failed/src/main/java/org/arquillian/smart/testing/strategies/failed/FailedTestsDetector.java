package org.arquillian.smart.testing.strategies.failed;
//tag::documentation[]

import java.io.File;
import java.util.Collection;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

public class FailedTestsDetector implements TestExecutionPlanner {

    static final String FAILED = "failed";

    private final File projectDir;
    private final FailedConfiguration strategyConfig;

    public FailedTestsDetector(File projectDir, Configuration configuration) {
        this.projectDir = projectDir;
        strategyConfig = (FailedConfiguration) configuration.getStrategyConfiguration(FAILED);
    }

    @Override
    public Collection<TestSelection> selectTestsFromNames(Iterable<String> testsToRun) { // <1>
        return getTests();
    }

    @Override
    public Collection<TestSelection> selectTestsFromClasses(Iterable<Class<?>> testsToRun) { // <2>
        return getTests();
    }

    public Collection<TestSelection> getTests() {
        InProjectTestReportLoader reportLoader = new InProjectTestReportLoader(new JavaSPILoader(), projectDir);
        return TestResultsFilter.getFailedTests(strategyConfig, reportLoader.loadTestResults());
    }

    @Override
    public String getName() {
        return FAILED;
    }
}
//end::documentation[]
