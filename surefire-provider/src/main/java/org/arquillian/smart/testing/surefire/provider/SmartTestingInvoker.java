package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.util.Set;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.SmartTesting;
import org.arquillian.smart.testing.configuration.Configuration;

public class SmartTestingInvoker {

    Set<TestSelection> invokeSmartTestingAPI(TestsToRun testsToRun, Configuration configuration, File projectDir) {
        return SmartTesting
            .with(className -> testsToRun.getClassByName(className) != null, configuration)
            .in(projectDir)
            .applyOnClasses(testsToRun);
    }
}
