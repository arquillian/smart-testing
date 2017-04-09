package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.util.List;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.vcs.git.NewFilesDetector;

public class TestStrategyApplier {

    private TestsToRun testsToRun;
    ProviderParametersParser paramParser;

    public TestStrategyApplier(TestsToRun testsToRun, ProviderParametersParser paramParser){
        this.testsToRun = testsToRun;
        this.paramParser = paramParser;
    }

    public TestsToRun apply(List<String> orderStrategy) {
        // here I should call the planner implementations using getPlannerForStrategy method
        return testsToRun;
    }

    private TestExecutionPlanner getPlannerForStrategy(String orderStrategy){
        File projectDir = new File(System.getProperty("user.dir"));

        if (orderStrategy == "new") {
            List<String> globPatterns = paramParser.getIncludes();
            globPatterns.addAll(paramParser.getExcludes());
            return new NewFilesDetector(projectDir, "", "", (String[]) globPatterns.toArray());

        } else if (orderStrategy == "changed") {

        }
        return null;
    }
}
