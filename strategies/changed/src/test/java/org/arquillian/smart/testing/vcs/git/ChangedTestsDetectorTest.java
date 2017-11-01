package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.scm.git.GitChangeResolver;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.vcs.git.GitRepositoryUnpacker.unpackRepository;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangedTestsDetectorTest {

    @Rule
    public final TemporaryFolder gitFolder = new TemporaryFolder();

    @Before
    public void unpack_repo() {
        // see repo.log for current commit list
        final URL repoBundle = Thread.currentThread().getContextClassLoader().getResource("repo.bundle");
        unpackRepository(gitFolder.getRoot().getAbsolutePath(), repoBundle.getFile());
    }

    @Test
    public void should_find_all_modified_tests_in_the_range_of_commits() throws Exception {
        // given
        Configuration configuration = createConfiguration("7699c2c", "04d04fe");
        final ChangedTestsDetector changedTestsDetector =
            new ChangedTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(),
                className -> className.endsWith("Test"), configuration);

        // when
        final Iterable<TestSelection> changedTests = changedTestsDetector.getTests();

        // then
        assertThat(changedTests).extracting(TestSelection::getClassName)
            .containsOnly("org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest");
    }

    @Test
    public void should_find_all_renamed_tests_in_the_range_of_commits() throws Exception {
        // given
        // 76b6069 renames test introduced in b4a8a3 - org.arquillian.smart.testing.vcs.git.EvenMoreDummyFilesDetectorTest
        // 6b4a8a3 introduces new test - org.arquillian.smart.testing.vcs.git.DummyFilesDetectorTest
        Configuration configuration = createConfiguration("6b4a8a3", "76b6069");
        final ChangedTestsDetector changedTestsDetector =
            new ChangedTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(),
                className -> className.endsWith("Test"), configuration);

        // when
        final Iterable<TestSelection> changedTests = changedTestsDetector.getTests();

        // then
        assertThat(changedTests).extracting(TestSelection::getClassName)
            .containsOnly("org.arquillian.smart.testing.vcs.git.EvenMoreDummyFilesDetectorTest");
    }

    @Test
    public void should_find_all_local_modified_tests_as_changed() throws IOException {
        // given
        Configuration configuration = createConfiguration("a4261d5", "1ee4abf");
        final Path testFile = Paths.get(gitFolder.getRoot().getAbsolutePath(),
            "core/src/test/java/org/arquillian/smart/testing/FilesTest.java");
        Files.write(testFile, "//This is a test".getBytes(), StandardOpenOption.APPEND);

        final ChangedTestsDetector changedTestsDetector =
            new ChangedTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(),
                className -> className.endsWith("Test"), configuration);

        // when
        final Collection<TestSelection> modifiedTests = changedTestsDetector.getTests();

        // then
        assertThat(modifiedTests).extracting(TestSelection::getClassName)
            .containsOnly("org.arquillian.smart.testing.FilesTest");
    }

    @Test
    public void should_find_modified_staged_tests_as_changed() throws IOException, GitAPIException {
        //given
        Configuration configuration = createConfiguration("7699c2c", "04d04fe");
        final Path testFile = Paths.get(gitFolder.getRoot().getAbsolutePath(),
            "core/src/test/java/org/arquillian/smart/testing/FilesTest.java");
        Files.write(testFile, "//This is a test".getBytes(), StandardOpenOption.APPEND);
        GitRepositoryOperations.addFile(gitFolder.getRoot(), testFile.toString());

        final ChangedTestsDetector changedTestsDetector =
            new ChangedTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(),
                className -> className.endsWith("Test"), configuration);

        // when
        final Collection<TestSelection> newTests = changedTestsDetector.getTests();

        // then
        assertThat(newTests).extracting(TestSelection::getClassName)
            .containsOnly("org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest",
                "org.arquillian.smart.testing.FilesTest");
    }

    @Test
    public void should_not_find_newly_added_tests_in_commit_range_as_changed() throws IOException {
        //given
        Configuration configuration = createConfiguration("a4261d5", "1ee4abf");
        final ChangedTestsDetector changedTestsDetector =
            new ChangedTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(),
                className -> className.endsWith("Test"), configuration);

        // when
        final Collection<TestSelection> modifiedTests = changedTestsDetector.getTests();

        // then
        assertThat(modifiedTests).extracting(TestSelection::getClassName)
            .doesNotContain("org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest");
    }

    @Test
    public void should_not_find_untracked_tests_as_changed() throws IOException {
        //given
        Configuration configuration = createConfiguration("7699c2c", "04d04fe");
        final File testFile = gitFolder.newFile("core.src.test.java.org.arquillian.smart.testing.CalculatorTest.java");
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

        final ChangedTestsDetector changedTestsDetector =
            new ChangedTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(),
                className -> className.endsWith("Test"), configuration);

        // when
        final Collection<TestSelection> changedTest = changedTestsDetector.getTests();

        // then
        assertThat(changedTest).extracting(TestSelection::getClassName)
            .containsOnly("org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest")
            .doesNotContain("org.arquillian.smart.testing.CalculatorTest");
    }

    private Configuration createConfiguration(String tail, String head){
        Configuration configuration = ConfigurationLoader.load(gitFolder.getRoot());
        configuration.getScm().getRange().setTail(tail);
        configuration.getScm().getRange().setHead(head);
        return configuration;
    }
}
