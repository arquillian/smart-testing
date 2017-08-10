package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import org.arquillian.smart.testing.filter.TestVerifier;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;
import org.arquillian.smart.testing.strategies.affected.detector.FileSystemTestClassDetector;

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
    public TestExecutionPlanner create(File projectDir, TestVerifier verifier, String[] globPatterns) {
        return new AffectedTestsDetector(new FileSystemTestClassDetector(projectDir, verifier), "", verifier);
    }

}
