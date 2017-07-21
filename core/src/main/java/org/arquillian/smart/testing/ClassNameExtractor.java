package org.arquillian.smart.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ClassNameExtractor {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^(?!\\s*//)\\s*(package)\\s+([\\w+.]+)\\s*;", Pattern.COMMENTS | Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    // FIXME this assumes we only support Java at this point
    public String extractFullyQualifiedName(final File sourceFile)  {
        final String absolutePath = sourceFile.getAbsolutePath();
        final String className = absolutePath.substring(absolutePath.lastIndexOf('/') + 1).replaceAll(".java", "");

        try (Stream<String> lines = Files.lines(Paths.get(sourceFile.getAbsolutePath()))) {
            Optional<MatchResult> pkgName = lines.flatMap(line -> {
                final Matcher matcher = PACKAGE_PATTERN.matcher(line);
                return matcher.find() ? Stream.of(matcher.toMatchResult()) : null;
            }).findFirst();
            return pkgName.map(matchResult -> matchResult.group(2) + "." + className).orElse(className);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to analyze source file " + sourceFile.getAbsolutePath(), e);
        }
    }

    public String extractFullyQualifiedName(String path) {
        return extractFullyQualifiedName(new File(path));
    }
}
