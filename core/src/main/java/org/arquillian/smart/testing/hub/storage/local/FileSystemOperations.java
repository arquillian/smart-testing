package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.logger.Log;

class FileSystemOperations {

    private static final Logger logger = Log.getLogger();

    static Path copyDirectory(Path src, Path dest, boolean catchException) {
        return copyDirectory(src, dest, file -> true, catchException);
    }

    static Path copyDirectory(Path src, Path dest, Predicate<File> fileFilter, boolean catchException) {
        if (!dest.toFile().exists()) {
            try {
                Files.createDirectories(dest);
            } catch (IOException e) {
                handleException(e,
                    String.format("An error occurred when the directory %s was being created.", dest),
                    catchException);
                return null;
            }
        }
        Arrays.stream(src.toFile().listFiles())
            .filter(fileFilter)
            .forEach(file -> {
                if (file.isDirectory()) {
                    copyDirectory(file.toPath(), dest.resolve(file.getName()), fileFilter, catchException);
                } else {
                    copyFile(file, dest, catchException);
                }
            });
        return dest;
    }

    static Path copyFile(File src, Path destDir, boolean catchException) {
        Path destination = destDir.resolve(src.getName());
        try {
            return Files.copy(src.toPath(), destination);
        } catch (IOException e) {
            handleException(e,
                String.format("An error occurred when the file %s was being copied to %s.", src, destination),
                catchException);
        }
        return null;
    }

    static void deleteFile(Path path, boolean catchException) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            handleException(e,
                String.format("An error occurred when trying to remove the file %s.", path),
                catchException);
        }
    }

    static void deleteDirectory(Path directory, boolean catchException) {
        if (directory.toFile().exists()) {
            try {
                Files
                    .walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> deleteFile(path, catchException));
            } catch (IOException e) {
                handleException(e,
                    String.format("An error occurred when trying to delete the directory %s.", directory),
                    catchException);
            }
        }
    }

    private static void handleException(IOException e, String message, boolean catchException) {
        if (catchException) {
            logger.warn(message.concat("See the error message: " + e.getMessage()));
        } else {
            throw new IllegalStateException(message, e);
        }
    }
}
