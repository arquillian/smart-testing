package org.arquillian.smart.testing.surefire.provider;

import java.util.List;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

public class TestStrategyApplier {

    private TestsToRun testsToRun;
    ProviderParametersParser paramParser;

    public TestStrategyApplier(TestsToRun testsToRun, ProviderParametersParser paramParser){
        this.testsToRun = testsToRun;
        this.paramParser = paramParser;
    }

    public TestsToRun apply(List<String> orderStrategy) {
        return testsToRun;
    }

    private TestExecutionPlanner getPlannerForStrategy(String orderStrategy){
        //if (orderStrategy == "changed") {
        //    return new GitChangesDetector()
        //} else if (orderStrategy == "affected") {
        //
        //}
        return null;
    }
}
