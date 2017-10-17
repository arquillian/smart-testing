package org.arquillian.smart.testing.scm.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.arquillian.smart.testing.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class GitChangeResolverWithoutGitTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GitChangeResolver gitChangeResolver;
    private File gitFolder;

    @Before
    public void createTempFolder() throws IOException {
        gitFolder = Files.createTempDirectory("junit-").toFile();
    }

    @After
    public void closeRepoAndDeleteTempFolder() throws Exception {
        this.gitChangeResolver.close();
        Files.deleteIfExists(Paths.get(gitFolder.toString()));
    }


    @Test
    public void should_not_applicable_when_git_repository_is_not_initialized()  {
        // given
        this.gitChangeResolver = new GitChangeResolver();

        // when
        final boolean applicable = gitChangeResolver.isApplicable(gitFolder);

        // then
        assertThat(applicable).isFalse();
    }

    @Test
    public void should_throw_exception_for_fetching_all_changes_when_git_repository_is_not_initialized() {
        // given
        this.gitChangeResolver = new GitChangeResolver();

        // then
        thrown.expect(IllegalStateException.class);

        // when
        gitChangeResolver.diff(gitFolder, Configuration.load(), "custom");
    }

}
