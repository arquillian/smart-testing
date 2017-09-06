package org.arquillian.smart.testing.strategies.affected.detector;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.arquillian.smart.testing.api.TestVerifier;

/**
 * Implementation that gets all tests using glob pattern () by default and using root directory as reference by default
 */
public class FileSystemTestClassDetector implements TestClassDetector {

    private final File rootDirectory;
    private final TestVerifier verifier;

    public FileSystemTestClassDetector(File rootDirectory, TestVerifier verifier) {
        this.rootDirectory = rootDirectory;
        this.verifier = verifier;
    }

    @Override
    public Set<File> detect() {
        try (Stream<Path> stream = Files.walk(rootDirectory.toPath())) {
            return stream
                .filter(path -> Files.isRegularFile(path) && isJavaFile(path) && verifier.isTest(path))
                .map(Path::toFile)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isJavaFile(Path path) {
        return path.toString().toLowerCase().endsWith(".java");
    }
}
