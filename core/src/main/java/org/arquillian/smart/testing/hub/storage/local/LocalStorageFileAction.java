package org.arquillian.smart.testing.hub.storage.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class LocalStorageFileAction extends LocalStorageAction {

    private final Path path;

    LocalStorageFileAction(Path path, boolean isDirectory) {
        super(path, isDirectory);
        this.path = path;
    }

    public Path create(byte[] bytes, OpenOption... options) throws IOException {
        Files.createDirectories(path.getParent());
        return Files.write(path, bytes, options);
    }
}
