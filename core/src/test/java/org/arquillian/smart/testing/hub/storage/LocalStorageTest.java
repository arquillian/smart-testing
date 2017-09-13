package org.arquillian.smart.testing.hub.storage;

import java.io.IOException;
import java.nio.file.Paths;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.hub.storage.local.LocalStorage.DIRECTORY_NAME;
import static org.arquillian.smart.testing.hub.storage.local.LocalStorage.EXECUTION_SUBDIRECTORY;
import static org.arquillian.smart.testing.hub.storage.local.LocalStorage.REPORTING_SUBDIRECTORY;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalStorageTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_create_smart_testing_execution_directory() throws IOException {
        // when
        new LocalStorage(folder.getRoot()).execution().directory("directory").create();

        // then
        assertThat(Paths.get(folder.getRoot().getAbsolutePath(), DIRECTORY_NAME, EXECUTION_SUBDIRECTORY, "directory"))
            .exists()
            .isDirectory();
    }

    @Test
    public void should_create_smart_testing_reporting_directory() throws IOException {
        // when
        new LocalStorage(folder.getRoot()).reporting().directory("directory").create();

        // then
        assertThat(Paths.get(folder.getRoot().getAbsolutePath(), DIRECTORY_NAME, REPORTING_SUBDIRECTORY, "directory"))
            .exists()
            .isDirectory();
    }

    @Test
    public void should_create_smart_testing_execution_file() throws IOException {
        // when
        new LocalStorage(folder.getRoot()).execution().file("file").create();

        // then
        assertThat(Paths.get(folder.getRoot().getAbsolutePath(), DIRECTORY_NAME, EXECUTION_SUBDIRECTORY, "file"))
            .exists()
            .isRegularFile();
    }

    @Test
    public void should_create_smart_testing_reporting_file() throws IOException {
        // when
        new LocalStorage(folder.getRoot()).reporting().file("file").create();

        // then
        assertThat(Paths.get(folder.getRoot().getAbsolutePath(), DIRECTORY_NAME, REPORTING_SUBDIRECTORY, "file"))
            .exists()
            .isRegularFile();
    }
}
