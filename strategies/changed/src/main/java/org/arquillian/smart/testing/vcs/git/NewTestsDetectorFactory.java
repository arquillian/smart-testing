package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

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
    public TestExecutionPlanner create(File projectDir, TestVerifier verifier) {
        return new NewTestsDetector(projectDir, verifier);
    }
}
