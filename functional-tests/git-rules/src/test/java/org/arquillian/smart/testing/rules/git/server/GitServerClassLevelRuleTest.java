package org.arquillian.smart.testing.rules.git.server;

import java.io.File;
import org.arquillian.smart.testing.rules.git.GitCloner;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GitServerClassLevelRuleTest {

    @ClassRule
    public static final GitServer gitServer = GitServer.fromBundle("repo.bundle")
        .fromBundle("launchpad", "saas-launchpad.bundle")
        .usingPort(6655)
        .create();

    private GitCloner gitCloner;

    @Test
    public void should_clone_repository_using_http_call_and_custom_port_through_rule() throws Exception {
        // when
        gitCloner = new GitCloner("http://localhost:6655/repo.bundle");
        final Repository repository = gitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "Jenkinsfile")).exists();
    }

    @Test
    public void should_clone_second_repository_using_http_call_and_custom_port_through_rule() throws Exception {
        // when
        gitCloner = new GitCloner("http://localhost:6655/launchpad");
        final Repository repository = gitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "config.yaml")).exists();
    }

    @After
    public void cleanupClonedRepository() {
        gitCloner.removeClone();
    }

}
