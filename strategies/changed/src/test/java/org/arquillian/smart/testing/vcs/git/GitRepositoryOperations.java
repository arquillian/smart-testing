package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

class GitRepositoryOperations {

    static void addFile(File projectLocation, String filePattern) throws IOException, GitAPIException {
        try (Repository repository = getRepository(projectLocation);
             Git git = new Git(repository)) {
                git.add().addFilepattern(filePattern).call();
        }
    }

    private static Repository getRepository(File projectLocation) throws IOException {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();

        return builder.readEnvironment().findGitDir(projectLocation).build();
    }

}
