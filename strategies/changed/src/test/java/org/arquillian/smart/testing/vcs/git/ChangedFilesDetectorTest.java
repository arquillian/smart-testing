package org.arquillian.smart.testing.vcs.git;

import java.net.URL;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.vcs.git.GitRepositoryUnpacker.unpackRepository;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangedFilesDetectorTest {

    @ClassRule
    public static TemporaryFolder gitFolder = new TemporaryFolder();

    @BeforeClass
    public static void unpack_repo() {
        final URL repoBundle = Thread.currentThread().getContextClassLoader().getResource("repo.bundle");
        unpackRepository(gitFolder.getRoot().getAbsolutePath(), repoBundle.getFile());
    }

    @Test
    public void should_find_all_new_classes_in_the_range_of_commits() throws Exception {
        // given
        final ChangedFilesDetector changedFilesDetector = new ChangedFilesDetector(gitFolder.getRoot(), "7699c2c", "04d04fe");

        // when
        final Iterable<String> changedTests = changedFilesDetector.getTests();

        // then
        assertThat(changedTests).containsExactly(NewFilesDetectorTest.class.getCanonicalName());
    }

    @Test
    public void should_find_none_new_classes_in_the_range_of_commits_when_not_matching_pattern() throws Exception {
        // given
        final ChangedFilesDetector changedFilesDetector = new ChangedFilesDetector(gitFolder.getRoot(), "7699c2c",
            "04d04fe", "**/*IntegrationTest.java");

        // when
        final Iterable<String> changedTests = changedFilesDetector.getTests();

        // then
        assertThat(changedTests).isEmpty();
    }

}
