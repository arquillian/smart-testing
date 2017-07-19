package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.arquillian.smart.testing.GitRunnerProperties;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;
import org.eclipse.jgit.api.Git;

import static org.arquillian.smart.testing.GitRunnerProperties.COMMIT;
import static org.arquillian.smart.testing.GitRunnerProperties.HEAD;
import static org.arquillian.smart.testing.GitRunnerProperties.PREVIOUS_COMMIT;
import static org.arquillian.smart.testing.GitRunnerProperties.getPrevCommitDefaultValue;

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
    public TestExecutionPlanner create(File projectDir, File testSourceDir) {
        final String previousCommit = System.getProperty(PREVIOUS_COMMIT, getPrevCommitDefaultValue());
        final String commit = System.getProperty(COMMIT, HEAD);

        return new ChangedFilesDetector(projectDir, previousCommit, commit, testSourceDir.getAbsolutePath() + "/**/*.java");
    }
}
