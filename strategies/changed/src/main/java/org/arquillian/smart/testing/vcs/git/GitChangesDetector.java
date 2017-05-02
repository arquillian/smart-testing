package org.arquillian.smart.testing.vcs.git;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

abstract class GitChangesDetector implements TestExecutionPlanner {

    private static final Logger logger = Logger.getLogger(GitChangesDetector.class.getName());

    private final File repoRoot;
    private final String previous;
    private final String head;
    private final List<String> globPatterns;
    private final GitDiffFetcher gitDiffFetcher;

    GitChangesDetector(File currentDir, String previous, String head, String... globPatterns) {
        this.previous = previous;
        this.head = head;
        this.repoRoot = findRepoRoot(currentDir);
        this.gitDiffFetcher = new GitDiffFetcher(currentDir);
        if (globPatterns.length > 0) {
            this.globPatterns = Arrays.asList(globPatterns);
        } else {
            this.globPatterns = Collections.singletonList("**/*Test.*");
        }
    }

    private File findRepoRoot(File currentDir) {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repo = builder.readEnvironment().findGitDir(currentDir).build()) {
            return repo.getDirectory().getParentFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected abstract boolean isMatching(DiffEntry diffEntry);

    @Override
    public Collection<String> getTests() {
        final List<DiffEntry> diffs = gitDiffFetcher.diff(previous, head);
        return extractEntries(diffs, this.repoRoot);
    }

    public Set<File> getFiles() {
        final List<DiffEntry> diffs = gitDiffFetcher.diff(previous, head);
        return extractFiles(diffs, this.repoRoot);
    }

    private Set<File> extractFiles(List<DiffEntry> diffs, File repoRoot) {
        return diffs.stream()
            .filter(this::isMatching)
            .map(diffEntry -> new File(repoRoot, diffEntry.getNewPath()))
            .collect(Collectors.toSet());
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
            .peek(test -> logger.log(Level.FINEST, String.format("%s test added because it has been added or changed between %s and %s Git commit", test, previous, head)))
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
