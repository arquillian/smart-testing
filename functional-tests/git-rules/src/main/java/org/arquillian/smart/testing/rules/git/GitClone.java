package org.arquillian.smart.testing.rules.git;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.jgit.lib.Repository;
import org.junit.rules.ExternalResource;

public class GitClone extends ExternalResource {

    private final GitCloner gitCloner;
    private Repository repository;

    // tag::git_clone_custom_repo[]
    public GitClone(String gitRepoUrl)
    // end::git_clone_custom_repo[]
    {
        try {
            this.gitCloner = new GitCloner(new URL(gitRepoUrl).toExternalForm());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public GitClone(URL gitRepo) {
        this.gitCloner = new GitCloner(gitRepo.toExternalForm());
    }

    protected void before() throws Throwable {
        this.repository = gitCloner.cloneRepositoryToTempFolder();
    }

    protected void after() {
        gitCloner.removeClone();
    }

    public File getGitRepoFolder() {
        return repository.getDirectory().getParentFile();
    }
}
