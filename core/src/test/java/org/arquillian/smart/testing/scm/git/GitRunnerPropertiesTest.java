package org.arquillian.smart.testing.scm.git;

import java.io.IOException;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;

import static org.arquillian.smart.testing.scm.git.GitRunnerProperties.COMMIT;
import static org.arquillian.smart.testing.scm.git.GitRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.git.GitRunnerProperties.LAST_COMMITS;
import static org.arquillian.smart.testing.scm.git.GitRunnerProperties.PREVIOUS_COMMIT;
import static org.arquillian.smart.testing.scm.git.GitRunnerProperties.getPrevCommitDefaultValue;
import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class GitRunnerPropertiesTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_get_previous_commit_and_commit_when_git_last_commits_is_set() throws IOException {
        // given
        System.setProperty(LAST_COMMITS, "3");

        // when
        final String previousCommit = System.getProperty(PREVIOUS_COMMIT, getPrevCommitDefaultValue());
        final String commit = System.getProperty(COMMIT, HEAD);

        // then
        assertThat(previousCommit).isEqualTo("HEAD~3");
        assertThat(commit).isEqualTo("HEAD");
    }

    @Test
    public void should_get_previous_commit_and_commit() throws IOException {
        // given
        System.setProperty(PREVIOUS_COMMIT, "32bd752");
        System.setProperty(COMMIT, "07b181b");

        // when
        final String previousCommit = System.getProperty(PREVIOUS_COMMIT, getPrevCommitDefaultValue());
        final String commit = System.getProperty(COMMIT, HEAD);

        // then
        assertThat(previousCommit).isEqualTo("32bd752");
        assertThat(commit).isEqualTo("07b181b");
    }

    @Test
    public void should_get_head_as_previous_commit_and_commit_when_no_property_set() throws IOException {

        // when
        final String previousCommit = System.getProperty(PREVIOUS_COMMIT, getPrevCommitDefaultValue());
        final String commit = System.getProperty(COMMIT, HEAD);

        // then
        assertThat(previousCommit).isEqualTo("HEAD~0");
        assertThat(commit).isEqualTo(HEAD);
    }
}
