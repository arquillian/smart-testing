package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

class GitDiffFetcher {

    private final File repoRoot;

    GitDiffFetcher(File repoRoot) {
        this.repoRoot = repoRoot;
    }

    List<DiffEntry> diff(String previous, String head) {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.readEnvironment().findGitDir(repoRoot).build();
             ObjectReader reader = repository.newObjectReader();
             Git git = new Git(repository)) {

            final ObjectId oldHead = repository.resolve(previous + "^{tree}");
            final ObjectId newHead = repository.resolve(head + "^{tree}");

            final CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, oldHead);
            final CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, newHead);

            return git.diff().setNewTree(newTree).setOldTree(oldTree).call();

        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }
}
