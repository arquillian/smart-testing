package org.arquillian.smart.testing.hub.storage.local;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class DuringExecutionLocalStorage extends AfterExecutionLocalStorage {

    public static final String SMART_TESTING_WORKING_DIRECTORY_NAME = ".smart-testing";
    public static final String TEMPORARY_SUBDIRECTORY = "temporary";
    private String rootDir;

    DuringExecutionLocalStorage(String rootDir) {
        super(rootDir);
        this.rootDir = rootDir;
    }

    public LocalStorageType temporary() {
        return new LocalStorageType(getPathTo(TEMPORARY_SUBDIRECTORY));
    }

    protected Path getPathTo(String subdirectory) {
        return Paths.get(rootDir, SMART_TESTING_WORKING_DIRECTORY_NAME, subdirectory);
    }

    public void purge(String targetDir) {
        Arrays.stream(getDirsToStore())
            .forEach(dirNameToStore -> {
                storeDirectory(dirNameToStore, targetDir);
            });
    }

    private void storeDirectory(String dirNameToStore, String targetDir) {
        Path dirToCopy = getPathTo(dirNameToStore);
        if (dirToCopy.toFile().exists()) {
            Path dirToStore = new LocalStorage(rootDir)
                .afterExecution(targetDir)
                .getPathTo(dirNameToStore);

            FileSystemOperations.copyDirectory(dirToCopy, dirToStore, true);
        }
        FileSystemOperations.deleteDirectory(Paths.get(rootDir, SMART_TESTING_WORKING_DIRECTORY_NAME), true);
    }
}
