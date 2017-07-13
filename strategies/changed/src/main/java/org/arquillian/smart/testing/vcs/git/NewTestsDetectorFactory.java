package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

import static org.arquillian.smart.testing.scm.git.GitRunnerProperties.COMMIT;
import static org.arquillian.smart.testing.scm.git.GitRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.git.GitRunnerProperties.PREVIOUS_COMMIT;
import static org.arquillian.smart.testing.scm.git.GitRunnerProperties.getPrevCommitDefaultValue;

public class NewTestsDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public String alias() {
        return "new";
    }

    @Override
    public boolean isFor(String name) {
        return alias().equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir, String[] globPatterns) {
        final String previousCommit = System.getProperty(PREVIOUS_COMMIT, getPrevCommitDefaultValue());
        final String commit = System.getProperty(COMMIT, HEAD);

        return new NewTestsDetector(projectDir, previousCommit, commit, globPatterns);
    }
}
