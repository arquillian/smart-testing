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

import static org.arquillian.smart.testing.rules.git.server.UrlNameExtractor.extractName;

public class GitCloner {

    private static final Logger LOGGER = Logger.getLogger(GitCloner.class.getName());

    private final String repositoryName;
    private final String repositoryUrl;
    private File targetFolder;

    public GitCloner(String repositoryName, String repositoryUrl) {
        this.repositoryName = repositoryName;
        this.repositoryUrl = repositoryUrl;
    }

    public GitCloner(String repositoryUrl) {
        this(extractName(repositoryUrl), repositoryUrl);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public Repository cloneRepositoryToTempFolder() throws GitAPIException, IOException {
        this.targetFolder = createTempFolder(repositoryName);
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

    private File createTempFolder(String repositoryName) throws IOException {
        return Files.createTempDirectory("git-cloned-repo-" + repositoryName + "-").toFile();
    }

}
