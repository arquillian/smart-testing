package org.arquillian.smart.testing.ftest.testbed.rules;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.junit.rules.ExternalResource;

public class GitClone extends ExternalResource {

    private static final Logger LOGGER = Logger.getLogger(GitClone.class.getName());

    private final String gitRepo;
    private String gitRepoFolder;
    private File tempFolder;

    public GitClone() {
        try {
            this.gitRepo = new URL("https://github.com/arquillian/smart-testing-dogfood-repo.git").toExternalForm();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public GitClone(URL gitRepo) {
        this.gitRepo = gitRepo.toExternalForm();
    }

    protected void before() throws Throwable {
        tempFolder = createTempFolder();
        cloneTestProject();
    }

    protected void after() {
        if (tempFolder != null) {
            try {
                Files.walk(tempFolder.toPath(), FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::deleteOnExit);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    String getGitRepoFolder() {
        return gitRepoFolder;
    }

    private File createTempFolder() throws IOException {
        File createdFolder = File.createTempFile("junit", "", null);
        createdFolder.delete();
        createdFolder.mkdir();

        return createdFolder;
    }

    private void cloneTestProject() throws Exception {
        gitRepoFolder = tempFolder.getAbsolutePath() + File.separator + getRepoName();
        cloneRepository(gitRepoFolder, gitRepo.replace("file:", "")); // if local we have to strip it, otherwise git clone won't work
    }

    private String getRepoName() {
        return this.gitRepo.substring(gitRepo.lastIndexOf('/') + 1)
            .replace(".git", "");
    }

    private static void cloneRepository(String repoTarget, String repo) throws GitAPIException, IOException {
        final Repository repository = Git.cloneRepository()
                    .setURI(repo)
                    .setDirectory(new File(repoTarget))
                    .setCloneAllBranches(true)
                .call()
            .getRepository();

        if (!repository.getFullBranch().endsWith("master")) {
            Git.wrap(repository)
                .checkout()
                    .setName("master")
                .call();
        }

        LOGGER.info("Cloned test repository to: " + repoTarget);
    }
}
