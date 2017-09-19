package org.arquillian.smart.testing.scm.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import static org.arquillian.smart.testing.scm.Change.add;
import static org.arquillian.smart.testing.scm.Change.modify;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.COMMIT;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.PREVIOUS_COMMIT;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.getPrevCommitDefaultValue;

public class GitChangeResolver implements ChangeResolver {

    private static final String ENSURE_TREE = "^{tree}";

    private static final Logger logger = Logger.getLogger();

    private final String previous;
    private final String head;
    private final File repoRoot;
    private final Git git;

    public GitChangeResolver() {
        this(Paths.get("").toAbsolutePath().toFile());
    }

    public GitChangeResolver(File projectDir) {
        this(projectDir,
            System.getProperty(PREVIOUS_COMMIT, getPrevCommitDefaultValue()),
            System.getProperty(COMMIT, HEAD));
    }

    public GitChangeResolver(File dir, String previous, String head) {
        this.previous = previous;
        this.head = head;
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            this.git = new Git(builder.readEnvironment().findGitDir(dir).build());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to find git repository for path " + dir.getAbsolutePath(), e);
        }
        this.repoRoot = git.getRepository().getDirectory().getParentFile();
    }

    @Override
    public void close() throws Exception {
        git.close();
    }

    @Override
    public Set<Change> diff() {
        final Set<Change> allChanges= new HashSet<>();

        allChanges.addAll(retrieveCommitsChanges());
        allChanges.addAll(retrieveUncommittedChanges());

        return allChanges;
    }

    @Override
    public boolean isApplicable() {
        try {
            final FileRepositoryBuilder builder = new FileRepositoryBuilder();
            builder.readEnvironment().findGitDir().build();
        } catch (IOException e) {
            logger.warn("Working directory is not git directory. Cause: %s", e.getMessage());
            return false;
        }
        return true;
    }

    private Set<Change> retrieveCommitsChanges() {
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            final ObjectId oldHead = git.getRepository().resolve(previous + ENSURE_TREE);
            final ObjectId newHead = git.getRepository().resolve(head + ENSURE_TREE);

            final CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, oldHead);
            final CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, newHead);

            final List<DiffEntry> commitDiffs = git.diff()
                    .setNewTree(newTree)
                    .setOldTree(oldTree)
                .call();
            return transformToChangeSet(findRenames(commitDiffs), repoRoot);
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    private Set<Change> retrieveUncommittedChanges() {
        final Set<Change> allChanges = new HashSet<>();

        final Status status;
        try {
            status = git.status().call();
        } catch (GitAPIException e) {
            throw new IllegalArgumentException(e);
        }

        allChanges.addAll(status.getModified()
            .stream()
            .map(location -> modify(repoRoot.getAbsolutePath(), location))
            .collect(Collectors.toSet()));

        allChanges.addAll(status.getChanged()
            .stream()
            .map(location -> modify(repoRoot.getAbsolutePath(), location))
            .collect(Collectors.toSet()));

        allChanges.addAll(status.getUntracked()
            .stream()
            .map(location -> add(repoRoot.getAbsolutePath(), location))
            .collect(Collectors.toSet()));

        allChanges.addAll(status.getAdded()
            .stream()
            .map(location -> add(repoRoot.getAbsolutePath(), location))
            .collect(Collectors.toSet()));

        return allChanges;
    }

    private List<DiffEntry> findRenames(List<DiffEntry> commitDiffs) throws IOException {
        final RenameDetector renameDetector = new RenameDetector(git.getRepository());
        renameDetector.addAll(commitDiffs);
        return renameDetector.compute();
    }

    private Set<Change> transformToChangeSet(List<DiffEntry> diffs, File repoRoot) {
        return diffs.stream()
            .map(diffEntry -> {
                final Path classLocation = Paths.get(repoRoot.getAbsolutePath(), diffEntry.getNewPath());
                final ChangeType changeType = ChangeType.valueOf(diffEntry.getChangeType().name());

                return new Change(classLocation, changeType);
            })
            .collect(Collectors.toSet());
    }

}
