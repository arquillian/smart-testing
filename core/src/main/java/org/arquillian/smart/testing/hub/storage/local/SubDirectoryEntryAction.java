package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SubDirectoryEntryAction {

    private final Path path;
    private final boolean isDirectory;

    public SubDirectoryEntryAction(Path path, boolean isDirectory) {
        this.path = path;
        this.isDirectory = isDirectory;
    }

    public Path create() throws IOException {
        if (!path.toFile().exists()) {
            if (isDirectory) {
                return Files.createDirectories(path);
            } else {
                return Files.createFile(path);
            }
        }
        return path;
    }

    public Path getPath() {
        return path;
    }

    public File getFile() {
        return path.toFile();
    }
}
