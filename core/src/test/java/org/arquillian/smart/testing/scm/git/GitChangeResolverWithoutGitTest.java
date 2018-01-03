package org.arquillian.smart.testing.scm.git;

import java.nio.file.Paths;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.Constants.CURRENT_DIR;
import static org.assertj.core.api.Assertions.assertThat;

public class GitChangeResolverWithoutGitTest {

    @Rule
    public TemporaryFolder  gitFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GitChangeResolver gitChangeResolver;

    @After
    public void closeRepo() throws Exception {
        this.gitChangeResolver.close();
    }


    @Test
    public void should_not_applicable_when_git_repository_is_not_initialized()  {
        // given
        this.gitChangeResolver = new GitChangeResolver();

        // when
        final boolean applicable = gitChangeResolver.isApplicable(gitFolder.getRoot());

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
        gitChangeResolver.diff(gitFolder.getRoot(), ConfigurationLoader.load(CURRENT_DIR), "custom");
    }

}
