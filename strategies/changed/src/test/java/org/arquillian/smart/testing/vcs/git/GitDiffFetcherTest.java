package org.arquillian.smart.testing.vcs.git;

import java.net.URL;
import java.util.List;
import org.eclipse.jgit.diff.DiffEntry;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.vcs.git.GitRepositoryUnpacker.unpackRepository;
import static org.assertj.core.api.Assertions.assertThat;

public class GitDiffFetcherTest {

    @ClassRule
    public static TemporaryFolder gitFolder = new TemporaryFolder();

    @BeforeClass
    public static void unpack_repo() {
        final URL repoBundle = Thread.currentThread().getContextClassLoader().getResource("repo.bundle");
        unpackRepository(gitFolder.getRoot().getAbsolutePath(), repoBundle.getFile());
    }

    @Test
    public void should_fetch_only_gitignore_in_diff_between_two_immediate_commits() throws Exception {
        // given
        final GitDiffFetcher diffFetcher = new GitDiffFetcher(gitFolder.getRoot());

        // when
        final List<DiffEntry> diff = diffFetcher.diff("32bd752", "07b181b");

        // then
        assertThat(diff).hasSize(1).extracting(DiffEntry::getNewPath).containsOnly(".gitignore");
    }

    @Test
    public void should_fetch_all_files_from_first_commit_to_given_hash() throws Exception {
        // given
        final GitDiffFetcher diffFetcher = new GitDiffFetcher(gitFolder.getRoot());
        final String firstCommit = "d923b3a";
        final String stubTestCommit = "1ee4abf";

        // when
        final List<DiffEntry> diff = diffFetcher.diff(firstCommit, stubTestCommit);

        // then
        assertThat(diff).hasSize(18);
    }



}
