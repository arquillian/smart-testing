package org.arquillian.smart.testing.ftest.testbed.rules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static java.util.stream.Collectors.toList;

public class TestBed implements TestRule {

    private Project project;

    public String getTargetRepoPerTestFolder() {
        return targetRepoPerTestFolder;
    }

    private String targetRepoPerTestFolder;

    public TestBed() {
    }

    public Project getProject() {
        return project;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return this.statement(statement, description);
    }

    private Statement statement(final Statement base, final Description description) {

        return new Statement() {
            public void evaluate() throws Throwable {
                targetRepoPerTestFolder = targetRepoPerTestFolder(description);
                initializeTestProject();
                try {
                    base.evaluate();
                } finally {
                    cleanup();
                }
            }
        };
    }

    private String targetRepoPerTestFolder(Description description) {
        return GitClone.GIT_REPO_FOLDER
            + "_"
            + description.getTestClass().getSimpleName()
            + "_"
            + description.getMethodName();
    }

    private Path createPerTestRepository() throws IOException {
        final Path source = Paths.get(GitClone.GIT_REPO_FOLDER);
        final Path target = Paths.get(targetRepoPerTestFolder);
        final List<Path> sources = Files.walk(source).collect(toList());
        final List<Path> targets = sources.stream().map(source::relativize).map(target::resolve)
            .collect(toList());
        for (int i = 0; i < sources.size(); i++) {
            Files.copy(sources.get(i), targets.get(i));
        }
        System.out.println("Cloned test repository to: " + target);
        return target;
    }

    private void initializeTestProject() throws IOException {
        final Path target = createPerTestRepository();
        this.project = new Project(target);
    }

    private void cleanup() throws Exception {
        this.project.close();
    }
}
