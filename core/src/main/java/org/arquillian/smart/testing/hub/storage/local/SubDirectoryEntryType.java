package org.arquillian.smart.testing.hub.storage.local;

import java.nio.file.Path;

public class SubDirectoryEntryType {

    private LocalStorage localStorage;
    private final Path rootDir;

    public SubDirectoryEntryType(LocalStorage localStorage, Path rootDir) {
        this.localStorage = localStorage;
        this.rootDir = rootDir;
    }

    public SubDirectoryFileAction file(String fileName){
        return new SubDirectoryFileAction(rootDir.resolve(fileName), false);
    }

    public SubDirectoryDirectoryAction directory(String dirName){
        return new SubDirectoryDirectoryAction(rootDir.resolve(dirName), true);
    }
}
