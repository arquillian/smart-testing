package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalStorage {

    public static final String SMART_TESTING_WORKING_DIRECTORY_NAME = ".smart-testing";
    public static final String SMART_TESTING_TARGET_DIRECTORY_NAME = "smart-testing";
    public static final String EXECUTION_SUBDIRECTORY = "execution";
    public static final String REPORTING_SUBDIRECTORY = "reporting";
    private String rootDir;

    public LocalStorage(String rootDir) {
        this.rootDir = rootDir;
    }

    public LocalStorage(File rootDir) {
        this.rootDir = rootDir.getAbsolutePath();
    }

    public SubDirectoryEntryType execution() {
        return new SubDirectoryEntryType(getPathTo(EXECUTION_SUBDIRECTORY));
    }

    public SubDirectoryEntryType reporting() {
        return new SubDirectoryEntryType(getPathTo(REPORTING_SUBDIRECTORY));
    }

    private Path getPathTo(String subdirectory) {
        return Paths.get(rootDir, SMART_TESTING_WORKING_DIRECTORY_NAME, subdirectory);
    }

    public void purge(String targetDir) {
        Path reporting = getPathTo(REPORTING_SUBDIRECTORY);
        if (reporting.toFile().exists()) {
            File target = new File(rootDir, "target");
            if (targetDir != null){
                target = new File(targetDir);
            }
            FileUtils.copyDirectory(reporting, target.toPath().resolve(SMART_TESTING_TARGET_DIRECTORY_NAME), true);
        }
        FileUtils.deleteDirectory(Paths.get(rootDir, SMART_TESTING_WORKING_DIRECTORY_NAME), true);
    }
}
