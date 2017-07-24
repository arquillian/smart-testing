package org.arquillian.smart.testing.ftest.testbed;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectPersistTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();


    @Test
    public void temp_test_projects_should_present_under_target_if_test_is_failing() {
        final Result result = JUnitCore.runClasses(ProjectPersistFail.class);

        assertThat(result.wasSuccessful()).isFalse();
        assertThat(new File("target/projects/smart-testing-dogfood-repo_ProjectPersistFail_should_fail")).isDirectory().exists();
    }
}
