package org.arquillian.smart.testing.strategies.affected.detector;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.arquillian.smart.testing.filter.TestVerifier;

/**
 * Implementation that gets all tests using glob pattern () by default and using root directory as reference by default
 */
public class FileSystemTestClassDetector implements TestClassDetector {

    private File rootDirectory;
    private TestVerifier verifier;

    public FileSystemTestClassDetector(File rootDirectory, TestVerifier verifier) {
        this.rootDirectory = rootDirectory;
        this.verifier = verifier;
    }

    @Override
    public Set<File> detect() {
        try (Stream<Path> stream = Files.walk(rootDirectory.toPath())) {
            return stream
                .filter(path -> !Files.isDirectory(path) && isJavaFile(path) && verifier.isTest(path))
                .map(Path::toFile)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isJavaFile(Path path) {
        return path.toString().toLowerCase().endsWith(".java");
    }
}
