package org.arquillian.smart.testing.rules.git;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

public class GitCloner {

    private static final Logger LOGGER = Logger.getLogger(GitCloner.class.getName());

    private final String repositoryUrl;
    private File targetFolder;

    public GitCloner(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public Repository cloneRepositoryToTempFolder() throws GitAPIException, IOException {
        this.targetFolder = createTempFolder();
        final Repository repository = Git.cloneRepository()
            .setURI(repositoryUrl)
            .setDirectory(targetFolder)
            .setCloneAllBranches(true)
            .setBranch("master")
            .call()
            .getRepository();

        LOGGER.info("Cloned test repository to: " + targetFolder);
        return repository;
    }

    public void removeClone() {
        if (targetFolder != null) {
            try {
                Files.walk(targetFolder.toPath(), FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
                targetFolder.delete();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private File createTempFolder() throws IOException {
        final String suffix = this.repositoryUrl.substring(repositoryUrl.lastIndexOf('/') + 1).replace(".git", "");
        final File createdFolder = File.createTempFile("git-cloned-repo", suffix, null);
        createdFolder.delete();
        createdFolder.mkdir();
        return createdFolder;
    }
}
