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
    public final GitServer gitServer = GitServer.bundlesFromDirectory(".")
        .usingAnyFreePort() // this way we can run this test in parallel
        .create();

    private GitCloner gitCloner;

    @Test
    public void should_clone_repository_using_http_call_and_custom_port_through_rule() throws Exception {
        // when
        gitCloner = new GitCloner("http://localhost:" + gitServer.getPort() + "/repo");
        final Repository repository = gitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "Jenkinsfile")).exists();
    }

    @Test
    public void should_clone_second_repository_using_http_call_custom_port_and_using_last_part_of_the_url_as_repo_name_through_rule() throws Exception {
        // when
        gitCloner = new GitCloner("http://localhost:" + gitServer.getPort() + "/can-be-anything/saas-launchpad");
        final Repository repository = gitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "config.yaml")).exists();
    }

    @After
    public void cleanupClonedRepository() {
        gitCloner.removeClone();
    }

}
