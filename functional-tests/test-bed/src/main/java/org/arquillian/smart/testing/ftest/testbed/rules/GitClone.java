package org.arquillian.smart.testing.ftest.testbed.rules;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.rules.ExternalResource;

public class GitClone extends ExternalResource {

    private static final String ORIGIN = "https://github.com/arquillian/smart-testing-dogfood-repo.git";
    private static final String REPO_NAME = ORIGIN.substring(ORIGIN.lastIndexOf('/') + 1).replace(".git", "");

    public static String GIT_REPO_FOLDER;

    public static final Logger LOGGER = Logger.getLogger(GitClone.class.getName());

    private File tempFolder;

    protected void before() throws Throwable {
        tempFolder = createTempFolder();
        cloneTestProject();
    }

    protected void after() {
        if (tempFolder != null) {
            recursiveDelete(tempFolder);
        }
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each);
            }
        }
        file.delete();
    }

    private File createTempFolder() throws IOException {
        File createdFolder = File.createTempFile("junit", "", null);
        createdFolder.delete();
        createdFolder.mkdir();

        return createdFolder;
    }

    private void cloneTestProject() throws Exception {
        GIT_REPO_FOLDER = tempFolder.getAbsolutePath() + File.separator + REPO_NAME;
        cloneRepository(GIT_REPO_FOLDER, ORIGIN);
    }

    private static void cloneRepository(String repoTarget, String repo) throws GitAPIException {
        Git.cloneRepository()
                .setURI(repo)
                .setDirectory(new File(repoTarget))
                .setCloneAllBranches(true)
            .call()
                .checkout()
                    .setName("master")
            .call();

        LOGGER.info("cloned test repository to: " + repoTarget);
    }
}
