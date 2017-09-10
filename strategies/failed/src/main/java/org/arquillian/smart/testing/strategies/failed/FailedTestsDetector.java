package org.arquillian.smart.testing.strategies.failed;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

public class FailedTestsDetector implements TestExecutionPlanner {

    private final TestReportLoader testReportLoader = new InProjectTestReportLoader(new JavaSPILoader());

    @Override
    public Collection<TestSelection> getTests() {
        return testReportLoader.loadTestResults()
            .stream()
            .map(result -> new TestSelection(result, "failed"))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getName() {
        return "failed";
    }
}
