package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.vcs.git.GitRepositoryUnpacker.unpackRepository;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangedFilesDetectorTest {

    @Rule
    public TemporaryFolder gitFolder = new TemporaryFolder();

    @Before
    public void unpack_repo() {
        final URL repoBundle = Thread.currentThread().getContextClassLoader().getResource("repo.bundle");
        unpackRepository(gitFolder.getRoot().getAbsolutePath(), repoBundle.getFile());
    }

    @Test
    public void should_find_all_modified_files_in_the_range_of_commits() throws Exception {
        // given
        final ChangedFilesDetector
            changedFilesDetector = new ChangedFilesDetector(gitFolder.getRoot(), "7699c2c", "04d04fe");

        // when
        final Iterable<String> changedTests = changedFilesDetector.getTests();

        // then
        assertThat(changedTests).containsOnly("org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest");
    }

    @Test
    public void should_find_none_new_classes_in_the_range_of_commits_when_not_matching_pattern() throws Exception {
        // given
        final ChangedFilesDetector
            changedFilesDetector = new ChangedFilesDetector(gitFolder.getRoot(), "7699c2c",
            "04d04fe", "**/*IntegrationTest.java");

        // when
        final Iterable<String> changedTests = changedFilesDetector.getTests();

        // then
        assertThat(changedTests).isEmpty();
    }

    @Test
    public void should_find_all_local_modified_files_as_changed() throws IOException {
        //given
        final Path testFile =
            Paths.get(gitFolder.getRoot().getAbsolutePath(),
                "core/src/test/java/org/arquillian/smart/testing/FilesTest.java");
        Files.write(testFile, "//This is a test".getBytes(), StandardOpenOption.APPEND);

        final ChangedFilesDetector
            changedFilesDetector = new ChangedFilesDetector(gitFolder.getRoot(), "a4261d5", "1ee4abf");

        //when
        final Collection<String> modifiedTests = changedFilesDetector.getTests();

        // then
        assertThat(modifiedTests).containsOnly("org.arquillian.smart.testing.FilesTest");
    }

    @Test
    public void should_find_modified_staged_files_as_changed() throws IOException, GitAPIException {
        //given
        final Path testFile =
            Paths.get(gitFolder.getRoot().getAbsolutePath(),
                "core/src/test/java/org/arquillian/smart/testing/FilesTest.java");
        Files.write(testFile, "//This is a test".getBytes(), StandardOpenOption.APPEND);
        GitRepositoryOperations.addFile(gitFolder.getRoot(), testFile.toString());

        final ChangedFilesDetector
            changedFilesDetector = new ChangedFilesDetector(gitFolder.getRoot(), "7699c2c", "04d04fe");

        // when
        final Collection<String> newTests = changedFilesDetector.getTests();

        // then
        assertThat(newTests).containsOnly("org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest",
            "org.arquillian.smart.testing.FilesTest");
    }

    @Test
    public void should_not_find_newly_added_files_in_commit_range_as_changed() throws IOException {
        //given
        final ChangedFilesDetector
            changedFilesDetector = new ChangedFilesDetector(gitFolder.getRoot(), "a4261d5", "1ee4abf");

        // when
        final Collection<String> modifiedTests = changedFilesDetector.getTests();

        // then
        assertThat(modifiedTests).doesNotContain("org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest");
    }

    @Test
    public void should_not_find_untracked_files_as_changed() throws IOException {
        //given
        final File testFile = gitFolder.newFile("core/src/test/java/org/arquillian/smart/testing/CalculatorTest.java");
        Files.write(testFile.toPath(), ("package org.arquillian.smart.testing;\n"
            + "\n"
            + "import org.junit.Assert;\n"
            + "import org.junit.Test;\n"
            + "\n"
            + "public class CalculatorTest {\n"
            + "\n"
            + "    @Test\n"
            + "    public void should_add_numbers() {\n"
            + "        Assert.assertEquals(6, 4 + 2);\n"
            + "    }\n"
            + "}").getBytes(), StandardOpenOption.APPEND);

        final ChangedFilesDetector
            changedFilesDetector = new ChangedFilesDetector(gitFolder.getRoot(), "7699c2c", "04d04fe");

        // when
        final Collection<String> changedTest = changedFilesDetector.getTests();

        // then
        assertThat(changedTest).containsOnly("org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest");
        assertThat(changedTest).doesNotContain("org.arquillian.smart.testing.CalculatorTest");
    }
}
