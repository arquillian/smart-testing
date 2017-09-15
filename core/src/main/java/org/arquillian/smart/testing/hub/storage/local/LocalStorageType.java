package org.arquillian.smart.testing.hub.storage.local;

import java.nio.file.Path;

public class LocalStorageType {

    private final Path rootDir;

    LocalStorageType(Path rootDir) {
        this.rootDir = rootDir;
    }

    public LocalStorageFileAction file(String fileName){
        return new LocalStorageFileAction(rootDir.resolve(fileName), false);
    }

    public LocalStorageDirectoryAction directory(String dirName){
        return new LocalStorageDirectoryAction(rootDir.resolve(dirName), true);
    }
}
