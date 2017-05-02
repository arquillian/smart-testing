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

/**
 * Implementation that gets all tests using glob pattern () by default and using root directory as reference by default
 */
public class FileSystemTestClassDetector implements TestClassDetector {

    private File rootDirectory;
    private List<String> globPatterns;

    public FileSystemTestClassDetector(File rootDirectory, String... globPatterns) {
        this.rootDirectory = rootDirectory;
        if (globPatterns.length > 0) {
            this.globPatterns = Arrays.asList(globPatterns);
        } else {
            this.globPatterns = Collections.singletonList("**/src/test/java/**/*Test.*");
        }
    }

    @Override
    public Set<File> detect() {

        final Set<File> tests = new HashSet<>();

        for (String pattern : globPatterns) {
            try {
                tests.addAll(findTests(rootDirectory, getPathMatcher(pattern)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return tests;
    }

    private Collection<? extends File> findTests(File rootDirectory, PathMatcher pathMatcher) throws IOException {
        try (Stream<Path> stream = Files.walk(rootDirectory.toPath())) {
            return stream
                .filter(pathMatcher::matches)
                .map(Path::toFile)
                .collect(Collectors.toList());
        }
    }

    private PathMatcher getPathMatcher(String pattern) {
        return FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }
}
