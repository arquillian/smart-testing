package org.arquillian.smart.testing.strategies.failed;
//tag::documentation[]
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

public class FailedTestsDetector implements TestExecutionPlanner {

    private final TestReportLoader testReportLoader = new InProjectTestReportLoader(new JavaSPILoader());

    @Override
    public Collection<TestSelection> getTests() { // <1>
        return testReportLoader.loadTestResults()
            .stream()
            .map(result -> new TestSelection(result, getName())) // <2>
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getName() {
        return "failed";
    }
}
//end::documentation[]
