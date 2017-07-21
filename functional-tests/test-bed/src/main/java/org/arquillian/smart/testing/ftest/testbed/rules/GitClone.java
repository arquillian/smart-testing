package org.arquillian.smart.testing.ftest.testbed.rules;

import java.io.File;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class GitClone implements TestRule {

    private static final String ORIGIN = "https://github.com/arquillian/smart-testing-dogfood-repo.git";
    private static final String REPO_NAME = ORIGIN.substring(ORIGIN.lastIndexOf('/') + 1).replace(".git", "");

    public static String GIT_REPO_FOLDER;
    private final TemporaryFolder tempFolder;

    public GitClone(TemporaryFolder tempFolder) {
        this.tempFolder = tempFolder;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                cloneTestProject();
                statement.evaluate();
            }
        };
    }

    private void cloneTestProject() throws Exception {
        GIT_REPO_FOLDER = tempFolder.getRoot().getAbsolutePath() + File.separator + REPO_NAME;
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

        System.out.println("cloned test repository to: " + repoTarget);
    }
}
