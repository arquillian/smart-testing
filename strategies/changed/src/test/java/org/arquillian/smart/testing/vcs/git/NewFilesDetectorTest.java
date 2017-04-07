package org.arquillian.smart.testing.vcs.git;

import java.net.URL;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.task.os.CommandTool;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class NewFilesDetectorTest {

    @ClassRule
    public static TemporaryFolder gitFolder = new TemporaryFolder();

    @BeforeClass
    public static void unpack_repo() {
        final URL repoBundle = Thread.currentThread().getContextClassLoader().getResource("repo.bundle");
        Spacelift.task(CommandTool.class)
            .command(new CommandBuilder("git")
                .parameters("clone", repoBundle.getFile(), "-b", "master",
                    gitFolder.getRoot().getAbsolutePath()).build())
            .execute().await();
    }

    @Test
    public void should_find_all_new_classes_in_the_range_of_commits() throws Exception {
        // given
        final NewFilesDetector newFilesDetector = new NewFilesDetector(gitFolder.getRoot(), "a4261d5", "1ee4abf");

        // when
        final Iterable<String> newTests = newFilesDetector.getTests();

        // then
        assertThat(newTests).containsExactly(NewFilesDetectorTest.class.getCanonicalName());
    }

    @Test
    public void should_find_none_new_classes_in_the_range_of_commits_when_not_matching_pattern() throws Exception {
        // given
        final NewFilesDetector newFilesDetector = new NewFilesDetector(gitFolder.getRoot(), "a4261d5", "1ee4abf", "**/*IntegrationTest.java");

        // when
        final Iterable<String> newTests = newFilesDetector.getTests();

        // then
        assertThat(newTests).isEmpty();
    }

}
