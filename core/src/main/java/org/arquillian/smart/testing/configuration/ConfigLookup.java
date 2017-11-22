package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.util.function.Function;

import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

class ConfigLookup {

    private final File executionRootDir;
    private final Function<File, Boolean> stopCondition;

    ConfigLookup(File executionRootDir, Function<File, Boolean> stopCondition) {
        this.executionRootDir = executionRootDir;
        this.stopCondition = stopCondition;
    }

    File getFirstDirWithConfigOrWithStopCondition() {
        return getFirstDirWithConfigOrWithStopCondition(executionRootDir);
    }

    private File getFirstDirWithConfigOrWithStopCondition(File projectDir) {
        if (stopCondition.apply(projectDir)) {
            return projectDir;
        }

        if (projectDir.isDirectory() && new File(projectDir, SMART_TESTING_YML).exists() || new File(projectDir,
            SMART_TESTING_YAML).exists()) {
            return projectDir;
        }

        return getFirstDirWithConfigOrWithStopCondition(projectDir.getParentFile());
    }
}
