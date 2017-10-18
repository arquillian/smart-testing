package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Class that takes care of storing/managing files and directories used and stored during the test execution.
 */
public class DuringExecutionLocalStorage extends AfterExecutionLocalStorage {

    public static final String SMART_TESTING_WORKING_DIRECTORY_NAME = ".smart-testing";
    public static final String TEMPORARY_SUBDIRECTORY = "temporary";
    private String rootDir;

    DuringExecutionLocalStorage(String rootDir) {
        super(rootDir);
        this.rootDir = rootDir;
    }

    /**
     * Opens an API for any action above files and directories that should be used only during the test execution and
     * shouldn't be available to the end user when the build is ended
     *
     * @return An instance of {@link LocalStorageType} that provides you an option to choose if you want to manage a file
     * or a directory.
     */
    public LocalStorageType temporary() {
        return new LocalStorageType(getPathTo(TEMPORARY_SUBDIRECTORY));
    }

    protected Path getPathTo(String subdirectory) {
        return Paths.get(rootDir, SMART_TESTING_WORKING_DIRECTORY_NAME, subdirectory);
    }

    /**
     * Copies all directories that should be stored after the test execution to the given {@code targetDir} directory.
     * If there is some directory with the same name present, then both directories are merged.
     * If the target directory does not exist or is not provided, then the directories are not moved anywhere.
     * <p>
     * When the directories are copied, then the whole {@link SMART_TESTING_WORKING_DIRECTORY_NAME} is removed
     * </p>
     */
    public void purge(String targetDir) {
        if (targetDir != null &&  new File(targetDir).exists()) {
            Arrays.stream(getDirsToStore())
                .forEach(dirNameToStore -> {
                    storeDirectory(dirNameToStore, targetDir);
                });
        }
        FileSystemOperations.deleteDirectory(Paths.get(rootDir, SMART_TESTING_WORKING_DIRECTORY_NAME), true);
    }

    private void storeDirectory(String dirNameToStore, String targetDir) {
        Path dirToCopy = getPathTo(dirNameToStore);
        if (dirToCopy.toFile().exists()) {
            Path dirToStore = new LocalStorage(rootDir)
                .afterExecution(targetDir)
                .getPathTo(dirNameToStore);

            FileSystemOperations.copyDirectory(dirToCopy, dirToStore, true);
        }
    }
}
