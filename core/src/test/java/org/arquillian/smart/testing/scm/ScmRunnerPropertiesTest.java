package org.arquillian.smart.testing.scm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.configuration.Scm;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;

import static org.arquillian.smart.testing.Constants.CURRENT_DIR;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;
import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ScmRunnerPropertiesTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_get_previous_commit_and_commit_when_git_last_commits_is_set() throws IOException {
        // given
        System.setProperty(SCM_LAST_CHANGES, "3");

        // when
        final Scm scm = ConfigurationLoader.load(CURRENT_DIR).getScm();

        // then
        assertThat(scm.getRange().getTail()).isEqualTo("HEAD~3");
        assertThat(scm.getRange().getHead()).isEqualTo("HEAD");
    }

    @Test
    public void should_get_previous_commit_and_commit() throws IOException {
        // given
        System.setProperty(SCM_RANGE_TAIL, "32bd752");
        System.setProperty(SCM_RANGE_HEAD, "07b181b");

        // when
        final Scm scm = ConfigurationLoader.load(CURRENT_DIR).getScm();

        // then
        assertThat(scm.getRange().getTail()).isEqualTo("32bd752");
        assertThat(scm.getRange().getHead()).isEqualTo("07b181b");
    }

    @Test
    public void should_get_head_as_previous_commit_and_commit_when_no_property_set() throws IOException {

        // when
        final Scm scm = ConfigurationLoader.load(CURRENT_DIR).getScm();

        // then
        assertThat(scm.getRange().getTail()).isEqualTo("HEAD~0");
        assertThat(scm.getRange().getHead()).isEqualTo(HEAD);
    }
}
