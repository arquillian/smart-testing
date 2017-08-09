package org.arquillian.smart.testing.ftest.testbed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectPersistTest {

    @Test
    public void should_store_project_under_test_to_two_separated_directories_when_the_same_test_fails_twice_in_a_row() throws IOException {
        final Result firstRun = JUnitCore.runClasses(ProjectPersistFail.class);
        final Result secondRun = JUnitCore.runClasses(ProjectPersistFail.class);

        assertThat(firstRun.wasSuccessful()).isFalse();
        assertThat(secondRun.wasSuccessful()).isFalse();
        // it's rather shallow check, but if it's not equal it means second execution of the test failed for different reasons
        assertThat(secondRun.getFailures()).hasSameSizeAs(firstRun.getFailures());

        assertThat(findPersistedProjects("smart-testing-dogfood-repo_ProjectPersistFail_should_fail")).hasSize(2);
    }

    @Test
    public void should_not_store_project_under_test_directory_when_test_is_passing() throws IOException {
        final Result result = JUnitCore.runClasses(ProjectPersistPass.class);

        assertThat(result.wasSuccessful()).isTrue();
        assertThat(findPersistedProjects("smart-testing-dogfood-repo_ProjectPersistPass_should_pass")).isEmpty();
    }

    private List<Path> findPersistedProjects(String projectName) throws IOException {
        return Files.walk(new File("target" + File.separator + "test-bed-executions").toPath(), 2)
            .filter(dir -> dir.toFile().getAbsolutePath().endsWith(projectName))
            .collect(Collectors.toList());
    }

    public static class ProjectPersistFail {

        @ClassRule
        public static final GitClone GIT_CLONE = new GitClone();

        @Rule
        public TestBed testBed = new TestBed(GIT_CLONE);

        @Test
        public void should_fail() throws Exception {
            Assert.assertFalse(true);
        }
    }

    public static class ProjectPersistPass {
        @ClassRule
        public static final GitClone GIT_CLONE = new GitClone();

        @Rule
        public TestBed testBed = new TestBed(GIT_CLONE);

        @Test
        public void should_pass() throws Exception {
            Assert.assertTrue(true);
        }
    }

}
