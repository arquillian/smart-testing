package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

public class JavaToClassLocation {

    private JavaToClassLocation() {
    }

    public static File transform(File javaLocation, List<String> globPatterns) {

        // TODO dirty method to know where the .class is located instead of .java Topic for next cabal?
        // Also setting URL instead of a File implies that in TestClassDetector we need to convert ALL tests to URL using Class.forName
        final File clazzFile;
        if (matchPatterns(javaLocation.getPath(), globPatterns)) {
            clazzFile =
                new File(
                    javaLocation.getAbsolutePath()
                        .replace("src/test/java", "target/test-classes")
                        .replace(".java", ".class")
                );
        } else {
            clazzFile =
                new File(
                    javaLocation.getAbsolutePath()
                        .replace("src/main/java", "target/classes")
                        .replace(".java", ".class")
                );
        }

        return clazzFile;
    }

    private static boolean matchPatterns(String path, List<String> globPatterns) {
        if (globPatterns != null) {
            for (final String globPattern : globPatterns) {
                if (matchPattern(path, globPattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchPattern(String path, String pattern) {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        return pathMatcher.matches(Paths.get(path));
    }
}
