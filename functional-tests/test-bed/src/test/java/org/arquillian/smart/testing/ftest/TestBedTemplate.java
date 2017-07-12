package org.arquillian.smart.testing.ftest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.task.os.CommandTool;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import static java.util.stream.Collectors.toList;

/**
 * This template is responsible for cloning source repository on which smart testing
 * provider will be tested and providing access to underlying project for further manipulations
 * and build execution.
 *
 * For each test method original repository which is cloned from defined location will be copied
 * to a new folder named after following pattern: [original-repo-name]_[class name]_[method_name].
 *
 * Each test has access to its own {@link Project} instance which allows to apply desired changes
 * before running the build.
 */
public abstract class TestBedTemplate {

    private static final String ORIGIN = "https://github.com/arquillian/smart-testing-dogfood-repo.git";
    private static final String REPO_NAME = ORIGIN.substring(ORIGIN.lastIndexOf('/') + 1).replace(".git", "");

    @ClassRule
    public static final TemporaryFolder TMP_FOLDER = new TemporaryFolder();

    @Rule
    public final TestName name = new TestName();

    private static String GIT_REPO_FOLDER;

    protected Project project;

    @BeforeClass
    public static void cloneTestProject() throws IOException {
        TMP_FOLDER.create();
        GIT_REPO_FOLDER = TMP_FOLDER.getRoot().getAbsolutePath() + File.separator + REPO_NAME;
        cloneRepository(GIT_REPO_FOLDER, ORIGIN);
    }

    @Before
    public void initializeTestProject() throws IOException {
        final Path target = createPerTestRepository();
        this.project = new Project(target);
    }

    @After
    public void cleanup() throws Exception {
        project.close();
    }

    private Path createPerTestRepository() throws IOException {
        final Path source = Paths.get(GIT_REPO_FOLDER);
        final Path target = Paths.get(targetRepoPerTestFolder());
        final List<Path> sources = Files.walk(source).collect(toList());
        final List<Path> targets = sources.stream().map(source::relativize).map(target::resolve)
            .collect(toList());
        for (int i = 0; i < sources.size(); i++) {
            Files.copy(sources.get(i), targets.get(i));
        }
        System.out.println("Cloned test repository to: " + target);
        return target;
    }

    private String targetRepoPerTestFolder() {
        return GIT_REPO_FOLDER + "_" + getClass().getSimpleName() + "_" + name.getMethodName();
    }

    static void cloneRepository(String repoTarget, String repo) {
        Spacelift.task(CommandTool.class)
            .command(new CommandBuilder("git")
                .parameters("clone", repo, "-b", "master",
                    repoTarget).build())
            .execute().await();
    }
}
