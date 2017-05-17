package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

class GitChangeResolver {

    private static final String ENSURE_TREE = "^{tree}";

    private final File repoRoot;

    GitChangeResolver(File repoRoot) {
        this.repoRoot = repoRoot;
    }

    List<DiffEntry> diff(String previous, String head) {
        try (Repository repository = getRepository();
             ObjectReader reader = repository.newObjectReader();
             Git git = new Git(repository)) {

            final ObjectId oldHead = repository.resolve(previous + ENSURE_TREE);
            final ObjectId newHead = repository.resolve(head + ENSURE_TREE);

            final CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, oldHead);
            final CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, newHead);

            return git.diff().setNewTree(newTree).setOldTree(oldTree).call();
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    Set<String> uncommitted() {

        try (Repository repository = getRepository();
             Git git = new Git(repository)) {

            return getUncommittedFiles(git.status().call());

        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    private Set<String> getUncommittedFiles(final Status status) {
        Set<String> notCommittedFiles = new HashSet<>();

        notCommittedFiles.addAll(status.getAdded());
        notCommittedFiles.addAll(status.getUntracked());
        notCommittedFiles.addAll(status.getModified());

        return notCommittedFiles;

    }

    private Repository getRepository() throws IOException {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.readEnvironment().findGitDir(repoRoot).build();
    }
}
