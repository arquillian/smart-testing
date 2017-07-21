package org.arquillian.smart.testing.ftest.testbed;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectPersistTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void deleteTempDir() {
        Arrays.stream(getAllTempDirs()).forEach(file -> {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                throw new IllegalStateException("failed to delete directory" + file.getName(), e);
            }
        });
    }

    @Test
    public void temp_test_projects_should_present_under_target_if_system_property_is_set() {
        System.setProperty("test.bed.project.persist", "true");
        final Result result = JUnitCore.runClasses(ProjectPersist.class);

        assertThat(result.wasSuccessful()).isTrue();
        assertThat(getAllTempDirs()).hasSize(1);
    }

    @Test
    public void temp_test_projects_should_not_present_under_target_if_system_property_is_not_set() {
        final Result result = JUnitCore.runClasses(ProjectPersist.class);

        assertThat(result.wasSuccessful()).isTrue();
        assertThat(getAllTempDirs()).hasSize(0);
    }

    private static File[] getAllTempDirs() {
        File target = new File("target");

        return target.listFiles(file -> file.getName().startsWith("junit"));
    }
}
