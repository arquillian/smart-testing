package org.arquillian.smart.testing.hub.storage.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * A class responsible for performing actions on a file.
 */
public class LocalStorageFileAction extends LocalStorageAction {

    private final Path path;

    LocalStorageFileAction(Path path, boolean isDirectory) {
        super(path, isDirectory);
        this.path = path;
    }

    /**
     * Creates a file set in previous steps and stores the given {@code bytes} with the given {@link OpenOption}s in it.
     *
     * @param bytes
     *     An array of bytes to store as a content of the file
     * @param options
     *     {@link OpenOption}s to be used for storing
     *
     * @return A {@link Path} to the stored file
     *
     * @throws IOException
     *     If anything bad happens
     */
    public Path create(byte[] bytes, OpenOption... options) throws IOException {
        Files.createDirectories(path.getParent());
        return Files.write(path, bytes, options);
    }
}
