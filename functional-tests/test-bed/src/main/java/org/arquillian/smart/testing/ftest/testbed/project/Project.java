package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class Project implements AutoCloseable {

    private final Path root;
    private final Repository repository;
    private final ProjectBuilder projectBuilder;

    public Project(Path root) throws IOException {
        this.root = root;
        this.repository = getRepository(root);
        this.projectBuilder = new ProjectBuilder(root);
    }

    private Repository getRepository(Path root) throws IOException {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.readEnvironment().findGitDir(root.toFile()).build();
    }

    public Path getRoot() {
        return root;
    }
    
    public String getMavenLog() {
        return projectBuilder.getMavenLog();
    }

    public boolean failed() {
        return projectBuilder.getBuiltProject().getMavenBuildExitCode() != 0;
    }

    public ProjectConfigurator configureSmartTesting() {
        return new ProjectConfigurator(this, root);
    }

    public Collection<TestResult> applyAsLocalChanges(String ... changeDescriptions) {
        return new ChangeApplier(repository).applyLocally(changeDescriptions);
    }

    public Collection<TestResult> applyAsCommits(String ... changeDescriptions) {
        return new ChangeApplier(repository).applyAsCommits(changeDescriptions);
    }

    @Override
    public void close() throws Exception {
        this.repository.close();
    }

    public ProjectBuilder build() {
        return this.projectBuilder;
    }

    public ProjectBuilder build(String ... projects) {
        projectBuilder.options().projects(projects);
        return projectBuilder;
    }

    /**
     *
     * Build relative path from project root to required submodule
     *
     * @param executionDir it is relative dir path from project root to submodule's pom.xml which you want to build
     * @return instance of {@link ProjectBuilder}
     *
     */
    public ProjectBuilder build(Path executionDir) {
        projectBuilder.options().executionDir(executionDir.toString());
        return projectBuilder;
    }
}
