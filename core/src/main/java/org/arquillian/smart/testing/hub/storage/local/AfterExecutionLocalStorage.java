package org.arquillian.smart.testing.hub.storage.local;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class that takes care of storing/managing files and directories permanently stored after the test execution.
 */
public class AfterExecutionLocalStorage {

    public static final String SMART_TESTING_TARGET_DIRECTORY_NAME = "smart-testing";
    public static final String REPORTING_SUBDIRECTORY = "reporting";

    private String rootDir;

    AfterExecutionLocalStorage(String rootDir) {
        this.rootDir = rootDir;
    }

    /**
     * Opens an API for any action above files and directories that should be used stored after the test execution and
     * should be available to the end user when the build is ended
     *
     * @return An instance of {@link LocalStorageType} that provides you an option to choose if you want to manage a file
     * or a directory.
     */
    public LocalStorageType toReporting() {
        return new LocalStorageType(getPathTo(REPORTING_SUBDIRECTORY));
    }

    protected Path getPathTo(String subdirectory) {
        return Paths.get(rootDir, SMART_TESTING_TARGET_DIRECTORY_NAME, subdirectory);
    }

    protected String[] getDirsToStore(){
        return new String[]{REPORTING_SUBDIRECTORY};
    }
}
