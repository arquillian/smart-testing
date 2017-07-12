package org.arquillian.smart.testing.scm;

// TODO we need to extract to an interface and maybe to Java SPI but for checking now this is enough

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class GitScmResolver implements AutoCloseable {

    private static final String ENSURE_TREE = "^{tree}";

    private static final Logger logger = Logger.getLogger(GitScmResolver.class);

    private final String previous;
    private final String head;
    private final File repoRoot;
    private final Git git;

    public GitScmResolver(File dir) {
        this(dir, "HEAD", "HEAD");
    }

    public GitScmResolver(File dir, String previous, String head) {
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

    public Set<Change> diff() {
        final Set<Change> allChanges= new HashSet<>();

        allChanges.addAll(retrieveCommitsChanges());
        allChanges.addAll(retrieveUncommittedChanges());

        return allChanges;
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
            .map(s -> new Change(new File(repoRoot, s), ChangeType.MODIFY))
            .collect(Collectors.toSet()));
        allChanges.addAll(status.getChanged()
            .stream()
            .map(s -> new Change(new File(repoRoot, s), ChangeType.MODIFY))
            .collect(Collectors.toSet()));
        allChanges.addAll(status.getUntracked()
            .stream()
            .map(s -> new Change(new File(repoRoot, s), ChangeType.ADD))
            .collect(Collectors.toSet()));
        allChanges.addAll(status.getAdded()
            .stream()
            .map(s -> new Change(new File(repoRoot, s), ChangeType.ADD))
            .collect(Collectors.toSet()));
        return allChanges;
    }

    private Set<Change> retrieveCommitsChanges() {
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            final ObjectId oldHead = git.getRepository().resolve(previous + ENSURE_TREE);
            final ObjectId newHead = git.getRepository().resolve(head + ENSURE_TREE);

            final CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, oldHead);
            final CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, newHead);

            final List<DiffEntry> commitDiffs = git.diff().setNewTree(newTree).setOldTree(oldTree).call();
            return extract(commitDiffs, repoRoot);
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    private Set<Change> extract(List<DiffEntry> diffs, File repoRoot) {
        return diffs.stream()
            .map(diffEntry -> {
                final File classLocation = new File(repoRoot, diffEntry.getNewPath());
                final ChangeType changeType = ChangeType.valueOf(diffEntry.getChangeType().name());

                return new Change(classLocation, changeType);
            })
            .collect(Collectors.toSet());
    }

}
