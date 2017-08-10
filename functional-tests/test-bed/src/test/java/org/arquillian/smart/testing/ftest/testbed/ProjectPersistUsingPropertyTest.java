package org.arquillian.smart.testing.ftest.testbed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class ProjectPersistUsingPropertyTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_store_project_under_test_directory_when_test_is_passing_but_property_is_set() throws IOException {
        System.setProperty("test.bed.project.persist", "true");

        final Result result = JUnitCore.runClasses(ProjectPersistAnotherPass.class);

        assertThat(result.wasSuccessful()).isTrue();
        assertThat(findPersistedProjects("smart-testing-dogfood-repo_ProjectPersistAnotherPass_should_pass")).hasSize(1);
    }

    private List<Path> findPersistedProjects(String projectName) throws IOException {
        return Files.walk(new File("target" + File.separator + "test-bed-executions").toPath(), 2)
            .filter(dir -> dir.toFile().getAbsolutePath().endsWith(projectName))
            .collect(Collectors.toList());
    }

    public static class ProjectPersistAnotherPass {
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
