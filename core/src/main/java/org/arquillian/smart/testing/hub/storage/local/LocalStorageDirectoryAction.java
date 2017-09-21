package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.io.IOException;
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


}
