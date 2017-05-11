package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

public class NewFilesDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public boolean isFor(String name) {
        return "new".equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir, String[] globPatterns) {
        final String previousCommit = System.getProperty("git.previous.commit", "HEAD");
        final String commit = System.getProperty("git.commit", "HEAD");
        return new NewFilesDetector(projectDir, previousCommit, commit, globPatterns);
    }

}
