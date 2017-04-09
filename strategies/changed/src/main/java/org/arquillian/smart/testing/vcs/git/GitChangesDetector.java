package org.arquillian.smart.testing.vcs.git;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.eclipse.jgit.diff.DiffEntry;

abstract class GitChangesDetector implements TestExecutionPlanner {

    private final File repoRoot;
    private final String previous;
    private final String head;
    private final List<String> globPatterns;
    private final GitDiffFetcher gitDiffFetcher;

    GitChangesDetector(File repoRoot, String previous, String head, String... globPatterns) {
        this.previous = previous;
        this.head = head;
        this.repoRoot = repoRoot;
        this.gitDiffFetcher = new GitDiffFetcher(repoRoot);
        if (globPatterns.length > 0) {
            this.globPatterns = Arrays.asList(globPatterns);
        } else {
            this.globPatterns = Collections.singletonList("**/*Test.*");
        }
    }

    protected abstract boolean isMatching(DiffEntry diffEntry);

    @Override
    public Iterable<String> getTests() {
        final List<DiffEntry> diffs = gitDiffFetcher.diff(previous, head);
        return extractEntries(diffs, this.repoRoot);
    }

    private List<String> extractEntries(List<DiffEntry> diffs, File repoRoot) {
        return diffs.stream()
            .filter(this::isMatching)
            .map(diffEntry -> {
                try {
                    final File sourceFile = new File(repoRoot, diffEntry.getNewPath());
                    return extractFullyQualifiedName(sourceFile);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .collect(Collectors.toList());
    }

    boolean matchPatterns(String path) {
        for (final String globPattern : this.globPatterns) {
            if (matchPattern(path, globPattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchPattern(String path, String pattern) {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        return pathMatcher.matches(Paths.get(path));
    }

    private String extractFullyQualifiedName(File sourceFile) throws FileNotFoundException {
        final CompilationUnit compilationUnit = JavaParser.parse(sourceFile);
        final Optional<ClassOrInterfaceDeclaration> newClass =
            compilationUnit.getClassByName(sourceFile.getName().replaceAll(".java", ""));
        return compilationUnit.getPackageDeclaration().get().getNameAsString() + "." + newClass.get().getNameAsString();
    }
}
