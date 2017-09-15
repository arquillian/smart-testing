package org.arquillian.smart.testing.hub.storage.local;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AfterExecutionLocalStorage {

    public static final String SMART_TESTING_TARGET_DIRECTORY_NAME = "smart-testing";
    public static final String REPORTING_SUBDIRECTORY = "reporting";

    private String rootDir;

    AfterExecutionLocalStorage(String rootDir) {
        this.rootDir = rootDir;
    }

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
