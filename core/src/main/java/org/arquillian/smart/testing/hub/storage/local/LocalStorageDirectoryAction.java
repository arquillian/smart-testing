package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * A class responsible for performing actions on a directory.
 */
public class LocalStorageDirectoryAction extends LocalStorageAction {

    private final Path path;

    LocalStorageDirectoryAction(Path path, boolean isDirectory) {
        super(path, isDirectory);
        this.path = path;
    }

    /**
     * Creates the directory set in previous steps and copies the content of the given {@code directoryToCopy} directory
     * to it. During the copy, all files and directories are filtered by the given {@code fileFilter}. If during the copy
     * any exception occurs, then it is checked the given {@code catchFileCopyException} parameter and if it is:
     * <ul>
     * <li><b>true</b> then it it just logs a warning and continues with the copy</li>
     * <li><b>false</b> then it throws {@link IllegalStateException}</li>
     * </ul>
     *
     * @param directoryToCopy
     *     A {@link Path} to a directory that should be copied
     * @param fileFilter
     *     A file filter that should be applied for all contained files and subdirectories
     * @param catchFileCopyException
     *     If any {@link IOException} should be caught and logged or rethrown using {@link IllegalStateException}
     *
     * @return A {@link Path} to the created directory
     */
    public Path create(Path directoryToCopy, Predicate<File> fileFilter, boolean catchFileCopyException) {
        return FileSystemOperations.copyDirectory(directoryToCopy, path, fileFilter, catchFileCopyException);
    }

    /**
     * Creates the directory set in previous steps and inside of this directory creates a file with the given name and the
     * given content.
     *
     * @param fileName
     *     A name of the file that should be created
     * @param bytes
     *     An array of bytes to store as a content of the file
     * @param options
     *     {@link OpenOption}s to be used for storing
     *
     * @return A path to the created file
     *
     * @throws IOException
     *     If anything bad happens
     */
    public Path createWithFile(String fileName, byte[] bytes, OpenOption... options) throws IOException {
        return new LocalStorageFileAction(path.resolve(fileName), false).create(bytes, options);
    }

}
