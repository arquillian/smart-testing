package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

public class AffectedChangesDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public String alias() {
        return "affected";
    }

    @Override
    public boolean isFor(String name) {
        return alias().equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir, TestVerifier verifier) {
        return new AffectedTestsDetector(projectDir, verifier);
    }

}
