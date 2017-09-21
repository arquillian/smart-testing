package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A class responsible for performing actions on a file or directory.
 */
public class LocalStorageAction {

    private final Path path;
    private final boolean isDirectory;

    LocalStorageAction(Path path, boolean isDirectory) {
        this.path = path;
        this.isDirectory = isDirectory;
    }

    /**
     * Creates the given file or directory set in previous steps. If the type is a file, then the parent directory (and
     * the whole path) is created as well.
     *
     * @return A {@link Path} of the created entry
     *
     * @throws IOException
     *     If anything bad happens
     */
    public Path create() throws IOException {
        if (!path.toFile().exists()) {
            if (isDirectory) {
                return Files.createDirectories(path);
            } else {
                Files.createDirectories(path.getParent());
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
