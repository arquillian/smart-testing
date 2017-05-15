package org.arquillian.smart.testing.vcs.git;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.vcs.git.GitRepositoryUnpacker.unpackRepository;
import static org.assertj.core.api.Assertions.assertThat;

public class NewFilesDetectorTest {

    @Rule
    public TemporaryFolder gitFolder = new TemporaryFolder();

    @Before
    public void unpack_repo() {
        final URL repoBundle = Thread.currentThread().getContextClassLoader().getResource("repo.bundle");
        unpackRepository(gitFolder.getRoot().getAbsolutePath(), repoBundle.getFile());
    }

    @Test
    public void should_find_all_new_classes_in_the_range_of_commits() throws Exception {
        // given
        final NewFilesDetector
            newFilesDetector = new NewFilesDetector(gitFolder.getRoot(), "a4261d5", "1ee4abf");

        // when
        final Iterable<String> newTests = newFilesDetector.getTests();

        // then
        assertThat(newTests).containsExactly(NewFilesDetectorTest.class.getCanonicalName());
    }

    @Test
    public void should_find_none_new_classes_in_the_range_of_commits_when_not_matching_pattern() throws Exception {
        // given
        final NewFilesDetector
            newFilesDetector = new NewFilesDetector(gitFolder.getRoot(), "a4261d5", "1ee4abf", "**/*IntegrationTest.java");

        // when
        final Iterable<String> newTests = newFilesDetector.getTests();

        // then
        assertThat(newTests).isEmpty();
    }

    @Test
    public void should_find_not_committed_files_as_new() throws IOException {
        //given
        final Path testFile =
            Paths.get(gitFolder.getRoot().getAbsolutePath(), "core/src/test/java/org/arquillian/smart/testing/FilesTest.java");

        Files.write(testFile, "//This is a test".getBytes(), StandardOpenOption.APPEND);

        final NewFilesDetector
            newFilesDetector = new NewFilesDetector(gitFolder.getRoot(), "a4261d5", "1ee4abf");

        // when
        final Collection<String> newTests = newFilesDetector.getTests();

        // then
        assertThat(newTests).containsExactly(NewFilesDetectorTest.class.getCanonicalName(), "org.arquillian.smart.testing.FilesTest");

    }

}
