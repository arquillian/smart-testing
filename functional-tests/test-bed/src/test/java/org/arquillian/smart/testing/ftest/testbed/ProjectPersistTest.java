package org.arquillian.smart.testing.ftest.testbed;

import java.io.File;
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
public class ProjectPersistTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_copy_failing_test_directory_in_target_if_test_is_failing() {
        final Result result = JUnitCore.runClasses(ProjectPersistFail.class);

        assertThat(result.wasSuccessful()).isFalse();
        assertThat(new File("target/projects/smart-testing-dogfood-repo_ProjectPersistFail_should_fail")).isDirectory().exists();
    }

    @Test
    public void temp_projects_should_copied_in_target_if_test_is_failing() {
        final Result firstRun = JUnitCore.runClasses(ProjectPersistFail.class);
        final Result secondRun = JUnitCore.runClasses(ProjectPersistFail.class);

        assertThat(firstRun.wasSuccessful()).isFalse();
        assertThat(secondRun.wasSuccessful()).isFalse();
        // it's rather shallow check, but if it's not equal it means second execution of the test failed for different reason
        assertThat(secondRun.getFailures()).hasSameSizeAs(firstRun.getFailures());
        assertThat(new File("target/projects/smart-testing-dogfood-repo_ProjectPersistFail_should_fail")).isDirectory().exists();
    }

    @Test
    public void temp_test_projects_should_not_copied_in_target_if_test_is_passing() {
        final Result result = JUnitCore.runClasses(ProjectPersistPass.class);

        assertThat(result.wasSuccessful()).isTrue();
        assertThat(new File("target/projects/smart-testing-dogfood-repo_ProjectPersistPass_should_pass")).doesNotExist();
    }

    @Test
    public void temp_test_projects_should_copied_in_target_if_test_is_passing_and_system_property_is_set() {
        System.setProperty("test.bed.project.persist", "true");

        final Result result = JUnitCore.runClasses(ProjectPersistPass1.class);

        assertThat(result.wasSuccessful()).isTrue();
        assertThat(new File("target/projects/smart-testing-dogfood-repo_ProjectPersistPass1_should_pass")).isDirectory();
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

    public static class ProjectPersistPass1 {
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
