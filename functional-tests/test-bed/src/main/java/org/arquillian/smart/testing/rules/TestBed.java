package org.arquillian.smart.testing.rules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toList;

public class TestBed implements TestRule {

    private final GitClone gitClone;
    private Project project;

    private String targetRepoPerTestFolder;

    public static final Logger LOGGER = Logger.getLogger(TestBed.class.getName());

    public TestBed(GitClone gitClone) {
        this.gitClone = gitClone;
    }
    
    public Project getProject() {
        return project;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return this.statement(statement, description);
    }

    private void succeeded(Description description) {
        if (isPersistFolderEnabled()) {
            copyTmpProjectToTarget();
        }
    }

    private void failed(Throwable e, Description description) {
        copyTmpProjectToTarget();
    }

    private void copyTmpProjectToTarget() {
        String path = "target" + File.separator + "test-bed-executions" + File.separator + Instant.now().toEpochMilli();
        final File projectDir = new File(path);
        if (!projectDir.exists()) {
            projectDir.mkdirs();
        }
        if (targetRepoPerTestFolder != null) {
            final Path source = Paths.get(targetRepoPerTestFolder);
            final Path target =
                Paths.get(path + targetRepoPerTestFolder.substring(targetRepoPerTestFolder.lastIndexOf("/")));
            try {
                final List<Path> sources = Files.walk(source).collect(toList());
                final List<Path> targets = sources.stream().map(source::relativize).map(target::resolve)
                    .collect(toList());
                for (int i = 0; i < sources.size(); i++) {
                    Files.copy(sources.get(i), targets.get(i));
                }
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            LOGGER.info("Copied test repository to: " + target);
        }
    }

    private Statement statement(final Statement base, final Description description) {

        return new Statement() {
            public void evaluate() throws Throwable {
                before(description);
                try {
                    List<Throwable> errors = new ArrayList<>();

                    try {
                        base.evaluate();
                        succeededQuietly(description, errors);
                    } catch (Throwable e) {
                        errors.add(e);
                        failedQuietly(e, description, errors);
                    }
                    MultipleFailureException.assertEmpty(errors);
                } finally {
                    after();
                }
            }
        };
    }

    private void before(Description description) throws IOException {
        targetRepoPerTestFolder = targetRepoPerTestFolder(description);
        initializeTestProject();
    }

    private void after() throws Exception {
        this.project.close();
    }

    private String targetRepoPerTestFolder(Description description) {
        return gitClone.getGitRepoFolder()
            + "_"
            + description.getTestClass().getSimpleName()
            + "_"
            + description.getMethodName();
    }

    private Path createPerTestRepository() throws IOException {
        final Path source = Paths.get(this.gitClone.getGitRepoFolder());
        final Path target = Paths.get(targetRepoPerTestFolder);
        final List<Path> sources = Files.walk(source).collect(toList());
        final List<Path> targets = sources.stream().map(source::relativize).map(target::resolve)
            .collect(toList());
        for (int i = 0; i < sources.size(); i++) {
            Files.copy(sources.get(i), targets.get(i));
        }
        LOGGER.info("Copied test repository to: " + target);
        return target;
    }

    private void initializeTestProject() throws IOException {
        final Path target = createPerTestRepository();
        this.project = new Project(target);
    }

    private void succeededQuietly(Description description, List<Throwable> errors) {
        try {
            succeeded(description);
        } catch (Throwable e) {
            errors.add(e);
        }
    }

    private void failedQuietly(Throwable e, Description description, List<Throwable> errors) {
        try {
            failed(e, description);
        } catch (Throwable e1) {
            errors.add(e1);
        }
    }

    private boolean isPersistFolderEnabled() {
        return Boolean.valueOf(getProperty("test.bed.project.persist", Boolean.toString(false)));
    }
}
