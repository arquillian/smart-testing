package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;

/**
 * A staring point for an API that takes care of local storage
 */
public class LocalStorage {

    private String rootDir;

    public LocalStorage(String rootDir) {
        this.rootDir = rootDir;
    }

    public LocalStorage(File rootDir) {
        this.rootDir = rootDir.getAbsolutePath();
    }

    /**
     * Creates an instance of {@link DuringExecutionLocalStorage} class that takes care of storing/managing files and
     * directories used during the test execution. All the files and dirs will be stored in the main directory of the
     * current project/module in {@link DuringExecutionLocalStorage#SMART_TESTING_WORKING_DIRECTORY_NAME} subdirectory.
     * <ul>
     * Use this method when you need to store something that:
     * <li>is used only during the test execution</li>
     * <li>should be also stored after the test execution (eg. reports) but you cannot store it in target directory right
     * now (before the clean phase)</li>
     * </ul>
     *
     * @return An instance of {@link DuringExecutionLocalStorage}
     */
    public DuringExecutionLocalStorage duringExecution() {
        return new DuringExecutionLocalStorage(rootDir);
    }

    /**
     * Creates an instance of {@link AfterExecutionLocalStorage} class that takes care of storing/managing files and
     * directories stored permanently after the test execution. All the files and dirs will be stored in the /target
     * directory of the current project/module in {@link AfterExecutionLocalStorage#SMART_TESTING_TARGET_DIRECTORY_NAME}
     * subdirectory.
     * <p>
     * Use this method when you need to store something that should be available also for the user when the build ends
     * (eg. reports)
     * </p>
     *
     * @return An instance of {@link AfterExecutionLocalStorage}
     */
    public AfterExecutionLocalStorage afterExecution() {
        return new AfterExecutionLocalStorage(rootDir + File.separator + "target");
    }

    /**
     * Creates an instance of {@link AfterExecutionLocalStorage} class that takes care of storing/managing files and
     * directories stored permanently after the test execution. All the files and dirs will be stored in the given {@code
     * pathToCustomTarget} directory and its {@link AfterExecutionLocalStorage#SMART_TESTING_TARGET_DIRECTORY_NAME}
     * subdirectory.
     * <p>
     * Use this method when you need to store something that should be available also for the user when the build ends
     * (eg. reports)
     * </p>
     *
     * @return An instance of {@link AfterExecutionLocalStorage}
     */
    public AfterExecutionLocalStorage afterExecution(String pathToCustomTarget) {
        if (pathToCustomTarget == null){
            return afterExecution();
        }
        return new AfterExecutionLocalStorage(pathToCustomTarget);
    }
}
