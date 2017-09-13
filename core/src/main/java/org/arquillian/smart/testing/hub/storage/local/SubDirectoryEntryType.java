package org.arquillian.smart.testing.hub.storage.local;

import java.nio.file.Path;

public class SubDirectoryEntryType {

    private final Path rootDir;

    public SubDirectoryEntryType(Path rootDir) {
        this.rootDir = rootDir;
    }

    public SubDirectoryFileAction file(String fileName){
        return new SubDirectoryFileAction(rootDir.resolve(fileName), false);
    }

    public SubDirectoryDirectoryAction directory(String dirName){
        return new SubDirectoryDirectoryAction(rootDir.resolve(dirName), true);
    }
}
