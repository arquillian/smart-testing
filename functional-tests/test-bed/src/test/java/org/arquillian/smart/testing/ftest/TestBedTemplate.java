package org.arquillian.smart.testing.ftest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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
    public static void cloneTestProject() throws Exception {
        GIT_REPO_FOLDER = cloneTestProject(ORIGIN, REPO_NAME);
    }

    private static String cloneTestProject(String origin, String repoName) throws Exception {

        TMP_FOLDER.create();
        final String tmpPath = TMP_FOLDER.getRoot().getAbsolutePath();
        String gitRepoName = tmpPath + File.separator + repoName;
        cloneRepository(gitRepoName, origin);

        return gitRepoName;
    }

    @Before
    public void initializeTestProject() throws IOException {
        initializeTestProject(GIT_REPO_FOLDER);
    }

    private void initializeTestProject(String gitRepoFolder) throws IOException {
        final Path target = createPerTestRepository(gitRepoFolder);
        this.project = new Project(target);
    }

    @After
    public void cleanup() throws Exception {
        project.close();
    }

    protected void configureOriginRepo(String origin) {
        final String repoName = origin.substring(origin.lastIndexOf('/') + 1).replace(".git", "");
        try {
            String gitRepoFolder = cloneTestProject(origin, repoName);
            initializeTestProject(gitRepoFolder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path createPerTestRepository(String gitRepoFolder) throws IOException {
        final Path source = Paths.get(gitRepoFolder);
        final Path target = Paths.get(targetRepoPerTestFolder(gitRepoFolder));
        final List<Path> sources = Files.walk(source).collect(toList());
        final List<Path> targets = sources.stream().map(source::relativize).map(target::resolve)
            .collect(toList());
        for (int i = 0; i < sources.size(); i++) {
            Files.copy(sources.get(i), targets.get(i));
        }
        System.out.println("Cloned test repository to: " + target);
        return target;
    }

    private String targetRepoPerTestFolder(String gitRepoFolder) {
        return gitRepoFolder + "_" + getClass().getSimpleName() + "_" + name.getMethodName();
    }

    static void cloneRepository(String repoTarget, String repo) throws GitAPIException {
        Git.cloneRepository()
                .setURI(repo)
                .setDirectory(new File(repoTarget))
                .setCloneAllBranches(true)
            .call()
                .checkout()
                    .setName("master")
            .call();
    }
}
