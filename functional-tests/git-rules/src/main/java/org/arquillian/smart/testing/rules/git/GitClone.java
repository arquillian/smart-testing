package org.arquillian.smart.testing.rules.git;

import java.io.File;
import java.net.URL;
import org.eclipse.jgit.lib.Repository;
import org.junit.rules.ExternalResource;

public class GitClone extends ExternalResource {

    private final GitCloner gitCloner;
    private Repository repository;

    // tag::git_clone_custom_repo[]
    public GitClone(String repositoryUrl)
    // end::git_clone_custom_repo[]
    {
        this.gitCloner = new GitCloner(repositoryUrl);
    }

    public GitClone(URL gitRepo) {
        this(gitRepo.toExternalForm());
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
