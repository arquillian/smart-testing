package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.arquillian.smart.testing.filter.TestVerifier;
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
    public TestExecutionPlanner create(File projectDir, TestVerifier verifier, String[] globPatterns) {
        return new ChangedFilesDetector(verifier);
    }
}
