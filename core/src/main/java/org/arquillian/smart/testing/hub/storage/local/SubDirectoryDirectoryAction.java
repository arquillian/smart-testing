package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Predicate;

public class SubDirectoryDirectoryAction extends SubDirectoryEntryAction {

    private final Path path;

    public SubDirectoryDirectoryAction(Path path, boolean isDirectory) {
        super(path, isDirectory);
        this.path = path;
    }

    public void create(Path directoryToCopy, Predicate<File> fileFilter, boolean catchFileCopyException) {
        FileSystemOperations.copyDirectory(directoryToCopy, path, fileFilter, catchFileCopyException);
    }


}
