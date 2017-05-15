package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

public class ChangedFilesDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public String alias() {
        return "changed";
    }

    @Override
    public boolean isFor(String name) {
        return alias().equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir, String[] globPatterns) {
        final String previousCommit = System.getProperty("git.previous.commit", "HEAD");
        final String commit = System.getProperty("git.commit", "HEAD");
        return new ChangedFilesDetector(projectDir, previousCommit, commit, globPatterns);
    }

}
