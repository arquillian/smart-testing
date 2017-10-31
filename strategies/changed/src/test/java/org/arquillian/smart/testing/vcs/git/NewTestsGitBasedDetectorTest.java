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

public class NewTestsGitBasedDetectorTest {

    @Rule
    public final TemporaryFolder gitFolder = new TemporaryFolder();

    @Before
    public void unpack_repo() {
        // see repo.log for current commit list
        final URL repoBundle = Thread.currentThread().getContextClassLoader().getResource("repo.bundle");
        unpackRepository(gitFolder.getRoot().getAbsolutePath(), repoBundle.getFile());
    }

    @Test
    public void should_find_all_new_classes_in_the_range_of_commits() throws Exception {
        // given
        Configuration configuration = createConfiguration("a4261d5", "1ee4abf");
        final NewTestsDetector newTestsDetector =
            new NewTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(), path -> true, configuration);

        // when
        final Collection<TestSelection> newTests = newTestsDetector.getTests();

        // then
        assertThat(newTests).extracting(TestSelection::getClassName)
            .containsOnly("org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest");
    }

    @Test
    public void should_find_local_untracked_files_as_new() throws IOException {
        //given
        Configuration configuration = createConfiguration("a4261d5", "1ee4abf");
        final File testFile = gitFolder.newFile("core/src/test/java/org/arquillian/smart/testing/CalculatorTest.java");
        Files.write(testFile.toPath(), getContentsOfClass().getBytes(), StandardOpenOption.APPEND);

        final NewTestsDetector newTestsDetector =
            new NewTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(), path -> true, configuration);

        // when
        final Collection<TestSelection> newTests = newTestsDetector.getTests();

        // then
        assertThat(newTests).extracting(TestSelection::getClassName)
            .containsOnly("org.arquillian.smart.testing.CalculatorTest",
                "org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest");
    }

    @Test
    public void should_find_local_newly_staged_files_as_new() throws IOException, GitAPIException {
        //given
        Configuration configuration = createConfiguration("a4261d5", "1ee4abf");
        final File testFile = gitFolder.newFile("core/src/test/java/org/arquillian/smart/testing/CalculatorTest.java");
        Files.write(testFile.toPath(), getContentsOfClass().getBytes(), StandardOpenOption.APPEND);

        GitRepositoryOperations.addFile(gitFolder.getRoot(), testFile.getAbsolutePath());

        final NewTestsDetector newTestsDetector =
            new NewTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(), path -> true, configuration);

        // when
        final Collection<TestSelection> newTests = newTestsDetector.getTests();

        // then
        assertThat(newTests).extracting(TestSelection::getClassName)
            .containsOnly("org.arquillian.smart.testing.CalculatorTest",
                "org.arquillian.smart.testing.vcs.git.NewFilesDetectorTest");
    }

    @Test
    public void should_not_find_local_modified_file_as_new_when_using_commit_range() throws IOException {
        //given
        Configuration configuration = createConfiguration("a4261d5", "1ee4abf");
        final Path testFile = Paths.get(gitFolder.getRoot().getAbsolutePath(),
            "core/src/test/java/org/arquillian/smart/testing/FilesTest.java");

        Files.write(testFile, "//This is a test".getBytes(), StandardOpenOption.APPEND);

        final NewTestsDetector newTestsDetector =
            new NewTestsDetector(new GitChangeResolver(), new NoopStorage(), gitFolder.getRoot(), path -> true, configuration);

        // when
        final Collection<TestSelection> newTests = newTestsDetector.getTests();

        // then
        assertThat(newTests).extracting(TestSelection::getClassName)
            .doesNotContain("org.arquillian.smart.testing.FilesTest");
    }

    private String getContentsOfClass() {
        return "package org.arquillian.smart.testing;\n"
            + "\n"
            + "import org.junit.Assert;\n"
            + "import org.junit.Test;\n"
            + "\n"
            + "public class CalculatorTest {\n"
            + "    @Test\n"
            + "    public void should_add_numbers() {\n"
            + "        Assert.assertEquals(6, 4 + 2);\n"
            + "    }\n"
            + "}";
    }

    private Configuration createConfiguration(String tail, String head){
        Configuration configuration = ConfigurationLoader.load(gitFolder.getRoot());
        configuration.getScm().getRange().setTail(tail);
        configuration.getScm().getRange().setHead(head);
        return configuration;
    }
}
