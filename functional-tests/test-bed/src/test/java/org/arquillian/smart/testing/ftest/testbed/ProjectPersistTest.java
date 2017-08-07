package org.arquillian.smart.testing.ftest.testbed;

import java.io.File;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class ProjectPersistTest {

    @Test
    public void temp_projects_should_copied_in_target_if_test_is_failing() {
        final Result result = JUnitCore.runClasses(ProjectPersistFail.class);

        assertThat(result.wasSuccessful()).isFalse();
        assertThat(new File("target/projects/smart-testing-dogfood-repo_ProjectPersistFail_should_fail")).isDirectory().exists();
    }

    @Test
    public void temp_test_projects_should_not_copied_in_target_if_test_is_passing() {
        final Result result = JUnitCore.runClasses(ProjectPersistPass.class);

        assertThat(result.wasSuccessful()).isTrue();
        assertThat(new File("target/projects/smart-testing-dogfood-repo_ProjectPersistPass_should_pass")).doesNotExist();
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
