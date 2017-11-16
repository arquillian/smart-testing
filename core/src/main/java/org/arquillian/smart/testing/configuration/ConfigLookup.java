package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.util.function.Function;

import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

class ConfigLookup {

    private final File executionRootDir;
    private final Function<File, Boolean> stopRecursiveLookup;

    ConfigLookup(File executionRootDir, Function<File, Boolean> stopRecursiveLookup) {
        this.executionRootDir = executionRootDir;
        this.stopRecursiveLookup = stopRecursiveLookup;
    }

    File getFirstDirWithConfigOrProjectRootDir() {
        return getFirstDirWithConfigOrProjectRootDir(executionRootDir);
    }

    private File getFirstDirWithConfigOrProjectRootDir(File projectDir) {
        if (stopRecursiveLookup.apply(projectDir)) {
            return projectDir;
        }

        if (projectDir.isDirectory() && new File(projectDir, SMART_TESTING_YML).exists() || new File(projectDir,
            SMART_TESTING_YAML).exists()) {
            return projectDir;
        }

        return getFirstDirWithConfigOrProjectRootDir(projectDir.getParentFile());
    }
}
