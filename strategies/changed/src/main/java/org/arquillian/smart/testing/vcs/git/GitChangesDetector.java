package org.arquillian.smart.testing.vcs.git;

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
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

abstract class GitChangesDetector implements TestExecutionPlanner {

    private static final Logger logger = Logger.getLogger(GitChangesDetector.class);

    private final String previous;
    private final String head;
    private final List<String> globPatterns;
    protected final File repoRoot;
    protected final GitChangeResolver gitChangeResolver;

    GitChangesDetector(File currentDir, String previous, String head, String... globPatterns) {
        this.previous = previous;
        this.head = head;
        this.repoRoot = findRepoRoot(currentDir);
        this.gitChangeResolver = new GitChangeResolver(currentDir);
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
        final List<DiffEntry> diffs = gitChangeResolver.diff(previous, head);
        return extractEntries(diffs, this.repoRoot);
    }

    public List<String> getLocalTests(Set<String> files) {
        final List<String> localTests = files.stream()
            .filter(file -> matchPatterns(new File(repoRoot, file).getAbsolutePath()))
            .map(file -> {
                try {
                    final File sourceFile = new File(repoRoot, file);
                    return new ClassNameExtractor().extractFullyQualifiedName(sourceFile);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .peek(
                test -> logger.finest("%s test added either because untracked or staged as new file", test))
            .collect(Collectors.toList());

        return localTests;
    }

    public Set<File> getFiles() {
        final List<DiffEntry> diffs = gitChangeResolver.diff(previous, head);
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
                    return new ClassNameExtractor().extractFullyQualifiedName(sourceFile);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .peek(test -> logger.finest("%s test added because it has been added or changed between %s and %s Git commit",
                test, previous, head)).collect(Collectors.toList());
    }

    public Set<File> filterLocalFiles(Set<String> localFiles) {
        return localFiles.stream()
            .filter(file -> matchPatterns(new File(repoRoot, file).getAbsolutePath()))
            .map(file -> new File(repoRoot, file))
            .collect(Collectors.toSet());
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
}
