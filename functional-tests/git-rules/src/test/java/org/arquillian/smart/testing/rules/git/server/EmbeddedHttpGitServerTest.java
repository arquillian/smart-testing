package org.arquillian.smart.testing.rules.git.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.rules.git.GitCloner;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class EmbeddedHttpGitServerTest {

    private final List<Function<Void, Void>> executeAfterTest = new ArrayList<>();

    @Test
    public void should_be_able_to_use_custom_port_and_clone_repository_using_http_call() throws Exception {
        // given
        final EmbeddedHttpGitServer gitServer = EmbeddedHttpGitServer.fromBundle("repo.bundle").usingPort(8989).create();
        final GitCloner gitCloner = new GitCloner("http://localhost:8989/any-name");
        executeAfterTest.add(v -> {
            gitServer.stop();
            gitCloner.removeClone();
            return v;
        });

        // when
        gitServer.start();
        final Repository repository = gitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "Jenkinsfile")).exists();
    }

    @Test
    public void should_be_able_to_use_custom_port_from_system_property_and_clone_repository_using_http_call() throws Exception {
        // given
        System.setProperty("git.server.port", "12345");
        final EmbeddedHttpGitServer gitServer = EmbeddedHttpGitServer.fromBundle("repo.bundle").create();
        final GitCloner gitCloner = new GitCloner("http://localhost:12345/any-other-name");
        executeAfterTest.add(v -> {
            gitServer.stop();
            gitCloner.removeClone();
            System.clearProperty("git.server.port");
            return v;
        });

        // when
        gitServer.start();
        final Repository repository = gitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(repository.getDirectory().getParent(), "Jenkinsfile")).exists();
    }

    @Test
    public void should_be_able_to_push_to_served_repository_on_the_default_port() throws Exception {
        // given
        final EmbeddedHttpGitServer gitServer = EmbeddedHttpGitServer.fromBundle("repo.bundle").create();
        final GitCloner gitCloner = new GitCloner("http://localhost:8765/any-other-name");
        final GitCloner secondGitCloner = new GitCloner("http://localhost:8765/any-other-name");
        executeAfterTest.add(v -> {
            gitServer.stop();
            gitCloner.removeClone();
            secondGitCloner.removeClone();
            return v;
        });

        gitServer.start();
        final Repository repository = gitCloner.cloneRepositoryToTempFolder();
        createNewFileAndPush(repository, "new-file.txt", "new file", "feat: new file");

        // when
        final Repository secondClone = secondGitCloner.cloneRepositoryToTempFolder();

        // then
        assertThat(new File(secondClone.getDirectory().getParent(), "new-file.txt")).exists();
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

    @After
    public void deferredCleanups() {
        executeAfterTest.forEach(cleanup -> cleanup.apply(null));
    }

}
