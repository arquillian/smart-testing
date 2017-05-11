package org.arquillian.smart.testing.spi;

import java.io.File;

public interface TestExecutionPlannerFactory {

    boolean isFor(String name);

    TestExecutionPlanner create(File projectDir, String[] globPatterns);

}
