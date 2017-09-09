package org.arquillian.smart.testing.rules.git.server;

import java.io.File;
import org.arquillian.smart.testing.rules.git.GitCloner;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GitServerRuleTest {

    @Rule
    public final GitServer gitServer = GitServer.fromBundle("repo.bundle").usingPort(9876).create();

    private final GitCloner gitCloner = new GitCloner("http://localhost:9876/any-name");

    @Test
    public void should_clone_repository_using_http_call_and_custom_port_through_rule() throws Exception {
        // when
        final Repository repository = gitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "Jenkinsfile")).exists();
    }

    @After
    public void cleanupClonedRepository() {
        gitCloner.removeClone();
    }

}
