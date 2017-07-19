package org.arquillian.smart.testing.spi;

import java.io.File;

public interface TestExecutionPlannerFactory {

    String alias();

    boolean isFor(String name);

    TestExecutionPlanner create(File projectDir, File testSourceDir);

}
