package org.arquillian.smart.testing.strategies.failed;
//tag::documentation[]
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

public class FailedTestsDetector implements TestExecutionPlanner {

    static final String FAILED = "failed";

    private final File projectDir;

    public FailedTestsDetector(File projectDir) {
        this.projectDir = projectDir;
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
        TestReportLoader testReportLoader = new InProjectTestReportLoader(new JavaSPILoader(), projectDir);
        return testReportLoader.loadTestResults()
            .stream()
            .map(result -> new TestSelection(result, getName())) // <3>
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getName() {
        return FAILED;
    }
}
//end::documentation[]
