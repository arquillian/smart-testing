package org.arquillian.smart.testing.rules.git.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.rules.git.GitCloner;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class EmbeddedHttpGitServerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final List<Function<Void, Void>> executeAfterTest = new ArrayList<>();

    private GitCloner gitCloner;
    private EmbeddedHttpGitServer gitServer;
    private Repository repository;

    @After
    public void cleanup() {
        executeAfterTest.forEach(cleanup -> cleanup.apply(null));
        if (repository != null) {
            repository.close();
        }
        if (gitCloner != null) {
            gitCloner.removeClone();
        }
        if (gitServer != null) {
            gitServer.stop();
        }
    }

    @Test
    public void should_be_able_to_use_custom_port_and_clone_repository_using_http_call() throws Exception {
        // given
        gitServer = EmbeddedHttpGitServer.fromBundle("test-repo", "booster-catalog.bundle").usingPort(8989).create();
        gitServer.start();

        // when
        gitCloner = new GitCloner("http://localhost:8989/test-repo");
        repository = gitCloner.cloneRepositoryToTempFolder(true);
        Git.wrap(repository).checkout().setName("next").call();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "RELEASE.adoc")).exists();
    }

    @Test
    public void should_be_able_to_clone_repository_using_http_call_ignoring_dot_git_suffix() throws Exception {
        // given
        gitServer = EmbeddedHttpGitServer.fromBundle("test-repo", "repo.bundle").usingPort(9922).create();
        gitServer.start();

        // when
        gitCloner = new GitCloner("http://localhost:9922/test-repo.git");
        repository = gitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "Jenkinsfile")).exists();
    }

    @Test
    public void should_be_able_to_use_custom_port_from_system_property_and_clone_repository_using_http_call()
        throws Exception {
        // given
        System.setProperty("git.server.port", "12345");
        gitServer = EmbeddedHttpGitServer.fromBundle("repo.bundle").create();
        executeAfterTest.add(v -> {
            System.clearProperty("git.server.port");
            return v;
        });
        gitServer.start();

        // when
        gitCloner = new GitCloner("http://localhost:12345/repo.bundle");
        repository = gitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "Jenkinsfile")).exists();
    }

    @Test
    public void should_be_able_to_push_to_served_repository_on_the_default_port() throws Exception {
        // given
        gitServer = EmbeddedHttpGitServer.fromBundle("das-repo","repo.bundle").create();
        gitCloner = new GitCloner("http://localhost:8765/das-repo");
        final GitCloner secondGitCloner = new GitCloner("http://localhost:8765/das-repo");
        executeAfterTest.add(v -> {
            secondGitCloner.removeClone();
            return v;
        });

        gitServer.start();
        repository = gitCloner.cloneRepositoryToTempFolder();
        createNewFileAndPush(repository, "new-file.txt", "new file", "feat: new file");

        // when
        final Repository secondClone = secondGitCloner.cloneRepositoryToTempFolder();
        executeAfterTest.add(v -> { secondClone.close(); return v; });

        // then
        assertThat(new File(secondClone.getDirectory().getParent(), "new-file.txt")).exists();
    }

    @Test
    public void should_be_able_to_serve_multiple_repositories() throws Exception {
        // given
        gitServer = EmbeddedHttpGitServer.fromBundle("das-repo","repo.bundle")
                                         .fromPath(Paths.get("src/test/resources/saas-launchpad.bundle"))
                                         .usingPort(5433)
                                         .create();
        gitServer.start();

        final GitCloner dasRepoCloner = new GitCloner("http://localhost:5433/das-repo");
        final GitCloner launchpadCloner = new GitCloner("http://localhost:5433/saas-launchpad.bundle");

        // when
        final Repository dasRepo = dasRepoCloner.cloneRepositoryToTempFolder();
        executeAfterTest.add(v -> { dasRepo.close(); return v; });
        final Repository launchpad = launchpadCloner.cloneRepositoryToTempFolder();
        executeAfterTest.add(v -> { launchpad.close(); return v; });

        // then
        assertThat(new File(dasRepo.getDirectory().getParent(), "Jenkinsfile")).exists();
        assertThat(new File(launchpad.getDirectory().getParent(), "config.yaml")).exists();
    }

    @Test
    public void should_fail_with_repository_not_found_when_trying_to_clone_non_existing_repo() throws Exception {
        // given
        gitServer = EmbeddedHttpGitServer.fromBundle("launchpad", "saas-launchpad.bundle")
            .usingPort(6432)
            .create();
        gitServer.start();

        expectedException.expect(TransportException.class);
        expectedException.expectMessage("Git repository not found");

        // when
        final GitCloner launchpadCloner = new GitCloner("http://localhost:6432/launchpadeeeee");
        final Repository launchpad = launchpadCloner.cloneRepositoryToTempFolder();

        // then
        // should fail
    }

    private void createNewFileAndPush(Repository repository, String newFile, String content, String commitMessage)
        throws IOException, GitAPIException {
        final File root = repository.getDirectory().getParentFile();
        Files.write(Paths.get(root.getPath(), newFile), content.getBytes());
        final Git git = Git.wrap(repository);
        git.add().addFilepattern(".").call();
        git.commit().setMessage(commitMessage).call();
        git.push().call();
    }
}
