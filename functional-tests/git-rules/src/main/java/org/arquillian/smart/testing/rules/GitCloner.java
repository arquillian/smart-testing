package org.arquillian.smart.testing.rules;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

class GitCloner {

    private static final Logger LOGGER = Logger.getLogger(GitCloner.class.getName());

    static void cloneRepository(String repoTarget, String repo) throws GitAPIException, IOException {
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
