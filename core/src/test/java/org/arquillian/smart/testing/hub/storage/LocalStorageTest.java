package org.arquillian.smart.testing.hub.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.hub.storage.local.LocalStorage.EXECUTION_SUBDIRECTORY;
import static org.arquillian.smart.testing.hub.storage.local.LocalStorage.REPORTING_SUBDIRECTORY;
import static org.arquillian.smart.testing.hub.storage.local.LocalStorage.SMART_TESTING_TARGET_DIRECTORY_NAME;
import static org.arquillian.smart.testing.hub.storage.local.LocalStorage.SMART_TESTING_WORKING_DIRECTORY_NAME;

public class LocalStorageTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private LocalStorage localStorage;

    @Before
    public void setLocalStorage() {
        localStorage = new LocalStorage(folder.getRoot());
    }

    @Test
    public void should_create_smart_testing_execution_directory() throws IOException {
        // when
        localStorage.execution().directory("directory").create();

        // then
        softly.assertThat(getSmartTestingSubdirectory(EXECUTION_SUBDIRECTORY, "directory"))
            .exists()
            .isDirectory();
    }

    @Test
    public void should_create_smart_testing_reporting_directory() throws IOException {
        // when
        localStorage.reporting().directory("directory").create();

        // then
        softly.assertThat(getSmartTestingSubdirectory(REPORTING_SUBDIRECTORY, "directory"))
            .exists()
            .isDirectory();
    }

    @Test
    public void should_create_smart_testing_execution_file() throws IOException {
        // when
        localStorage.execution().file("file").create();

        // then
        softly.assertThat(getSmartTestingSubdirectory(EXECUTION_SUBDIRECTORY, "file"))
            .exists()
            .isRegularFile();
    }

    @Test
    public void should_create_smart_testing_reporting_file() throws IOException {
        // when
        localStorage.reporting().file("file").create();

        // then
        softly.assertThat(getSmartTestingSubdirectory(REPORTING_SUBDIRECTORY, "file"))
            .exists()
            .isRegularFile();
    }

    @Test
    public void should_create_file_with_content() throws IOException {
        // when
        localStorage.reporting().file("file").create("content".getBytes());

        // then
        softly.assertThat(getSmartTestingSubdirectory(REPORTING_SUBDIRECTORY, "file"))
            .hasContent("content");
    }

    @Test
    public void should_copy_directory() throws IOException {
        //given
        Path toCopy = folder.newFolder().toPath();
        feedDummyDirectory(toCopy);

        // when
        localStorage.execution().directory("copied").create(toCopy, f -> true, false);

        // then
        Path copied = getSmartTestingSubdirectory(EXECUTION_SUBDIRECTORY, "copied");
        assertThatDirectoriesHaveSameContent(copied, toCopy);
    }

    @Test
    public void should_purge_smart_testing_directory_and_move_reporting_subdirectory() throws IOException {
        // given
        Path toCopy = folder.newFolder().toPath();
        feedDummyDirectory(toCopy);
        localStorage.execution().directory("exec-copy").create(toCopy, f -> true, false);
        localStorage.reporting().directory("report-copy").create(toCopy, f -> true, false);
        File target = folder.newFolder();

        // when
        localStorage.purge(target.getAbsolutePath());

        // then
        Path stDirectory = Paths.get(folder.getRoot().getAbsolutePath(), SMART_TESTING_WORKING_DIRECTORY_NAME);
        softly.assertThat(stDirectory).doesNotExist();

        List<Path> execCopyDirs = Files
            .walk(folder.getRoot().toPath())
            .filter(path -> path.toFile().getName().equals("exec-copy"))
            .collect(Collectors.toList());
        softly.assertThat(execCopyDirs).as("There should be no exec-copy dir present").isEmpty();

        File stTargetDir = new File(target, SMART_TESTING_TARGET_DIRECTORY_NAME);
        softly.assertThat(target.listFiles()).hasSize(1).contains(stTargetDir);

        File reportCopy = new File(stTargetDir, "report-copy");
        softly.assertThat(stTargetDir.listFiles()).hasSize(1).contains(reportCopy);

        assertThatDirectoriesHaveSameContent(reportCopy.toPath(), toCopy);
    }

    private void assertThatDirectoriesHaveSameContent(Path actualDir, Path expectedDir) {
        softly.assertThat(actualDir).exists().isDirectory();
        Arrays
            .stream(expectedDir.toFile().listFiles())
            .map(File::toPath)
            .forEach(expectedFile -> {
                Path actualFile = actualDir.resolve(expectedFile.getFileName());
                if (expectedFile.toFile().isDirectory()) {
                    assertThatDirectoriesHaveSameContent(actualFile, expectedFile);
                } else {
                    softly.assertThat(actualFile).exists().isRegularFile().hasSameContentAs(expectedFile);
                }
            });
    }

    private void feedDummyDirectory(Path dirRoot) throws IOException {
        IntStream.range(1, 10).forEach(i -> createDummyFile(dirRoot, "file" + i));
        Path subdirectory = Files.createDirectories(dirRoot.resolve(dirRoot.getFileName() + "subdirectory"));
        IntStream.range(1, 10).forEach(i -> createDummyFile(subdirectory, "file" + i));
    }

    private Path createDummyFile(Path directory, String fileName) {
        Path file = directory.resolve(fileName);
        try {
            Files.write(file, fileName.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return file;
    }

    private Path getSmartTestingSubdirectory(String subdirectory, String fileName) {
        return Paths.get(folder.getRoot().getAbsolutePath(), SMART_TESTING_WORKING_DIRECTORY_NAME, subdirectory,
            fileName);
    }
}
