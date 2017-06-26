package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    Set<String> newChanges() {

        try (Repository repository = getRepository();
             Git git = new Git(repository)) {

            return getNewChangesFromUntrackedAndStagedArea(git.status().call());
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    Set<String> modifiedChanges() {

        try (Repository repository = getRepository();
             Git git = new Git(repository)) {

             return getModifiedChangesFromNonStagedAndStagedArea(git.status().call());
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    private Set<String> getNewChangesFromUntrackedAndStagedArea(final Status status) {
        Set<String> newFiles = new HashSet<>();

        newFiles.addAll(status.getAdded());
        newFiles.addAll(status.getUntracked());

        return newFiles;
    }

    private Set<String> getModifiedChangesFromNonStagedAndStagedArea(final Status status) {
        Set<String> modifiedFiles = new HashSet<>();

        modifiedFiles.addAll(status.getChanged());
        modifiedFiles.addAll(status.getModified());

        return modifiedFiles;
    }

    private Repository getRepository() throws IOException {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.readEnvironment().findGitDir(repoRoot).build();
    }
}
