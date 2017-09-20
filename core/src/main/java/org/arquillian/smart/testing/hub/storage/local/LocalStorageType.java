package org.arquillian.smart.testing.hub.storage.local;

import java.nio.file.Path;

/**
 * A class providing an option of choosing between file and directory the next action will be performed on
 */
public class LocalStorageType {

    private final Path rootDir;

    LocalStorageType(Path rootDir) {
        this.rootDir = rootDir;
    }

    /**
     * Creates an instance of {@link LocalStorageFileAction} that allows you to perform actions on the given file.
     *
     * @param fileName
     *     A file name of the file the next action will be performed on
     *
     * @return An instance of {@link LocalStorageFileAction}
     */
    public LocalStorageFileAction file(String fileName){
        return new LocalStorageFileAction(rootDir.resolve(fileName), false);
    }

    /**
     * Creates an instance of {@link LocalStorageDirectoryAction} that allows you to perform actions on the given directory.
     *
     * @param dirName A directory name of the directory the next action will be performed on
     *
     * @return An instance of {@link LocalStorageDirectoryAction}
     */
    public LocalStorageDirectoryAction directory(String dirName){
        return new LocalStorageDirectoryAction(rootDir.resolve(dirName), true);
    }
}
