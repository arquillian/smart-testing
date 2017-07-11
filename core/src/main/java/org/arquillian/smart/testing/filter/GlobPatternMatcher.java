package org.arquillian.smart.testing.filter;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class GlobPatternMatcher {

    public static boolean matchPatterns(String path, String ... globPatterns) {
        if (globPatterns.length == 0) {
            return true;
        }

        for (final String globPattern : globPatterns) {
            if (matchPattern(path, globPattern)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchPattern(String path, String pattern) {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        return pathMatcher.matches(Paths.get(path));
    }

    public static boolean matchPatterns(Path path, String ... globPatterns) {
        return matchPatterns(path.toAbsolutePath().toString(), globPatterns);
    }
}
