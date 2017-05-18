package org.arquillian.smart.testing.vcs.git;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.vcs.git.GitRepositoryUnpacker.unpackRepository;
import static org.assertj.core.api.Assertions.assertThat;

public class GitChangeResolverTest {

    @Rule
    public TemporaryFolder gitFolder = new TemporaryFolder();

    @Before
    public void unpack_repo() {
        final URL repoBundle = Thread.currentThread().getContextClassLoader().getResource("repo.bundle");
        unpackRepository(gitFolder.getRoot().getAbsolutePath(), repoBundle.getFile());
    }

    @Test
    public void should_fetch_only_gitignore_in_diff_between_two_immediate_commits() throws Exception {
        // given
        final GitChangeResolver gitChangeResolver = new GitChangeResolver(gitFolder.getRoot());

        // when
        final List<DiffEntry> diff = gitChangeResolver.diff("32bd752", "07b181b");

        // then
        assertThat(diff).hasSize(1).extracting(DiffEntry::getNewPath).containsOnly(".gitignore");
    }

    @Test
    public void should_fetch_all_files_from_first_commit_to_given_hash() throws Exception {
        // given
        final GitChangeResolver gitChangeResolver = new GitChangeResolver(gitFolder.getRoot());
        final String firstCommit = "d923b3a";
        final String stubTestCommit = "1ee4abf";

        // when
        final List<DiffEntry> diff = gitChangeResolver.diff(firstCommit, stubTestCommit);

        // then
        assertThat(diff).hasSize(18);
    }

    @Test
    public void should_fetch_all_untracked_files() throws IOException {
        // given
        gitFolder.newFile("untracked.txt");
        final GitChangeResolver gitChangeResolver = new GitChangeResolver(gitFolder.getRoot());

        // when
        final Set<String> notCommitted = gitChangeResolver.uncommitted();

        // then
        assertThat(notCommitted).hasSize(1)
            .containsExactly("untracked.txt");
    }

    @Test
    public void should_fetch_all_added_files() throws IOException, GitAPIException {
        // given
        gitFolder.newFile("newadd.txt");
        final GitChangeResolver gitChangeResolver = new GitChangeResolver(gitFolder.getRoot());
        GitRepositoryOperations.addFile(gitFolder.getRoot(), "newadd.txt");

        // when
        final Set<String> notCommitted = gitChangeResolver.uncommitted();

        // then
        assertThat(notCommitted).hasSize(1)
            .containsExactly("newadd.txt");
    }

    @Test
    public void should_fetch_all_modified_files() throws IOException {
        // given
        final GitChangeResolver gitChangeResolver = new GitChangeResolver(gitFolder.getRoot());
        final Path readme = Paths.get(gitFolder.getRoot().getAbsolutePath(), "README.adoc");
        Files.write(readme, "More".getBytes(), StandardOpenOption.APPEND);

        // when
        final Set<String> notCommitted = gitChangeResolver.uncommitted();

        // then
        assertThat(notCommitted).hasSize(1)
            .containsExactly("README.adoc");
    }

}
