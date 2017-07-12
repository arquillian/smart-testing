package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

import static org.arquillian.smart.testing.scm.GitRunnerProperties.COMMIT;
import static org.arquillian.smart.testing.scm.GitRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.GitRunnerProperties.PREVIOUS_COMMIT;
import static org.arquillian.smart.testing.scm.GitRunnerProperties.getPrevCommitDefaultValue;

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
        final String previousCommit = System.getProperty(PREVIOUS_COMMIT, getPrevCommitDefaultValue());
        final String commit = System.getProperty(COMMIT, HEAD);

        return new ChangedFilesDetector(projectDir, previousCommit, commit, globPatterns);
    }
}
