package org.arquillian.smart.testing.scm.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.configuration.Scm;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import static java.lang.String.format;
import static org.arquillian.smart.testing.scm.Change.add;
import static org.arquillian.smart.testing.scm.Change.modify;

public class GitChangeResolver implements ChangeResolver {

    private static final String WRONG_COMMIT_ID_EXCEPTION = "Commit id '%s' is not found in %s Git repository";
    private static final String ENSURE_TREE = "^{tree}";

    private static final Logger logger = Log.getLogger();
    private Git git;

    @Override
    public void close() throws Exception {
        closeGitIfExists();
    }

    private void closeGitIfExists() {
        if (git != null) {
            git.close();
        }
    }

    @Override
    public Set<Change> diff(File projectDir, Configuration configuration) {
        Scm scm;
        if (configuration == null){
            scm = ConfigurationLoader.loadPrecalculated(projectDir).getScm();
        } else {
            scm = configuration.getScm();
        }

        return diff(projectDir, scm.getRange().getTail(), scm.getRange().getHead());
    }

    @Override
    public Collection<Change> diff(File projectDir, Configuration configuration, String strategy) {
        if (isApplicable(projectDir)) {
            return diff(projectDir, configuration);
        } else {
            throw new IllegalStateException(
                String.format("strategy %s needs scm to be initialized. Git is not initialized. "
                    + "Please initialize git using `git init`", strategy));
        }
    }

    Set<Change> diff(File projectDir, String previous, String head) {
        buildGit(projectDir);

        File repoRoot = git.getRepository().getDirectory().getParentFile();
        final Set<Change> allChanges= new HashSet<>();
        if (isAnyCommitExists()) {
            allChanges.addAll(retrieveCommitsChanges(previous, head, repoRoot));
        }
        allChanges.addAll(retrieveUncommittedChanges(repoRoot));

        return allChanges;
    }

    @Override
    public boolean isApplicable(File projectDir) {
        return fileRepositoryBuilder(projectDir).getGitDir() != null;
    }

    private FileRepositoryBuilder fileRepositoryBuilder(File currentGitDir) {
        return new FileRepositoryBuilder().readEnvironment().findGitDir(currentGitDir);
    }

    private void buildGit(File projectDir){
        closeGitIfExists();
        try {
            git = new Git(fileRepositoryBuilder(projectDir).build());
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to find git repository for path " + projectDir.getAbsolutePath(), e);
        }
    }

    private boolean isAnyCommitExists() {
        try {
            final ObjectId head = git.getRepository().resolve("HEAD" + ENSURE_TREE);
            return head != null;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Set<Change> retrieveCommitsChanges(String previous, String head, File repoRoot) {
        final Repository repository = git.getRepository();
        try (ObjectReader reader = repository.newObjectReader()) {
            final ObjectId oldHead = repository.resolve(previous + ENSURE_TREE);
            final ObjectId newHead = repository.resolve(head + ENSURE_TREE);
            validateCommitExists(oldHead, previous, repository);
            validateCommitExists(newHead, head, repository);

            final CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, oldHead);
            final CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, newHead);

            final List<DiffEntry> commitDiffs = git.diff().setNewTree(newTree).setOldTree(oldTree).call();
            return transformToChangeSet(reduceToRenames(commitDiffs), repoRoot);
        } catch (MissingObjectException e) {
            throw new IllegalArgumentException(format(WRONG_COMMIT_ID_EXCEPTION, e.getObjectId().getName(), repository.getDirectory().getAbsolutePath()));
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    private void validateCommitExists(ObjectId retrievedId, String id, Repository repository) {
        if (retrievedId == null) {
            throw new IllegalArgumentException(format(WRONG_COMMIT_ID_EXCEPTION, id, repository.getDirectory().getAbsolutePath()));
        }
    }

    private Set<Change> retrieveUncommittedChanges(File repoRoot) {
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

    /**
     * By default jgit sees renames as ADDs and DELETEs. For finding renames we use {@link RenameDetector} which tracks changes
     * and if they much similarity score (default 60%) treats them as renames
     *
     * @param commitDiffs diffs to analyze
     * @return original list reduced by ADDs/DELETEs which are in fact RENAMEs
     * @throws IOException
     */
    private List<DiffEntry> reduceToRenames(final Collection<DiffEntry> commitDiffs) throws IOException {
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
