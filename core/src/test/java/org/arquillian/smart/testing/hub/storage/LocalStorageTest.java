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

import static org.arquillian.smart.testing.hub.storage.local.AfterExecutionLocalStorage.REPORTING_SUBDIRECTORY;
import static org.arquillian.smart.testing.hub.storage.local.AfterExecutionLocalStorage.SMART_TESTING_TARGET_DIRECTORY_NAME;
import static org.arquillian.smart.testing.hub.storage.local.DuringExecutionLocalStorage.SMART_TESTING_WORKING_DIRECTORY_NAME;
import static org.arquillian.smart.testing.hub.storage.local.DuringExecutionLocalStorage.TEMPORARY_SUBDIRECTORY;

public class LocalStorageTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private LocalStorage localStorage;

    @Before
    public void setLocalStorage() {
        localStorage = new LocalStorage(folder.getRoot());
    }

    @Test
    public void should_create_smart_testing_temporary_directory_in_basedir() throws IOException {
        // when
        localStorage.duringExecution().temporary().directory("directory").create();

        // then
        softly.assertThat(getSmartTestingSubdirectory(TEMPORARY_SUBDIRECTORY, "directory"))
            .exists()
            .isDirectory();
    }

    @Test
    public void should_create_smart_testing_reporting_directory_in_basedir() throws IOException {
        // when
        localStorage.duringExecution().toReporting().directory("directory").create();

        // then
        softly.assertThat(getSmartTestingSubdirectory(REPORTING_SUBDIRECTORY, "directory"))
            .exists()
            .isDirectory();
    }

    @Test
    public void should_create_smart_testing_temporary_file_in_basedir() throws IOException {
        // when
        localStorage.duringExecution().temporary().file("file").create();

        // then
        softly.assertThat(getSmartTestingSubdirectory(TEMPORARY_SUBDIRECTORY, "file"))
            .exists()
            .isRegularFile();
    }

    @Test
    public void should_create_smart_testing_reporting_file_in_basedir() throws IOException {
        // when
        localStorage.duringExecution().toReporting().file("file").create();

        // then
        softly.assertThat(getSmartTestingSubdirectory(REPORTING_SUBDIRECTORY, "file"))
            .exists()
            .isRegularFile();
    }

    @Test
    public void should_create_file_with_content() throws IOException {
        // when
        localStorage.duringExecution().toReporting().file("file").create("content".getBytes());

        // then
        softly.assertThat(getSmartTestingSubdirectory(REPORTING_SUBDIRECTORY, "file"))
            .hasContent("content");
    }

    @Test
    public void should_create_directory_and_file_with_content_in_it() throws IOException {
        // when
        localStorage.duringExecution().temporary().directory("directory")
            .createWithFile("file", "content".getBytes());

        // then
        softly.assertThat(getSmartTestingSubdirectory(TEMPORARY_SUBDIRECTORY, "directory").resolve("file"))
            .hasContent("content");
    }

    @Test
    public void should_copy_directory() throws IOException {
        //given
        Path toCopy = folder.newFolder().toPath();
        feedDummyDirectory(toCopy);

        // when
        localStorage.duringExecution().temporary().directory("copied").create(toCopy, f -> true, false);

        // then
        Path copied = getSmartTestingSubdirectory(TEMPORARY_SUBDIRECTORY, "copied");
        assertThatDirectoriesHaveSameContent(copied, toCopy);
    }

    @Test
    public void should_purge_smart_testing_directory_and_move_reporting_subdirectory() throws IOException {
        // given
        Path toCopy = folder.newFolder().toPath();
        feedDummyDirectory(toCopy);
        localStorage.duringExecution().temporary().directory("exec-copy").create(toCopy, f -> true, false);
        localStorage.duringExecution().toReporting().directory("report-copy").create(toCopy, f -> true, false);
        File target = folder.newFolder();

        // when
        localStorage.duringExecution().purge(target.getAbsolutePath());

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

        File reportingTargetDir = new File(stTargetDir, REPORTING_SUBDIRECTORY);
        softly.assertThat(stTargetDir.listFiles()).hasSize(1).contains(reportingTargetDir);

        File reportCopy = new File(reportingTargetDir, "report-copy");
        softly.assertThat(reportingTargetDir.listFiles()).hasSize(1).contains(reportCopy);

        assertThatDirectoriesHaveSameContent(reportCopy.toPath(), toCopy);
    }

    @Test
    public void should_create_smart_testing_reporting_directory_in_target() throws IOException {
        // when
        localStorage.afterExecution().toReporting().directory("directory").create();

        // then
        softly.assertThat(getSmartTestingSubdirectoryInTarget(REPORTING_SUBDIRECTORY, "directory"))
            .exists()
            .isDirectory();
    }

    @Test
    public void should_create_smart_testing_reporting_file_in_target() throws IOException {
        // when
        localStorage.afterExecution().toReporting().file("file").create();

        // then
        softly.assertThat(getSmartTestingSubdirectoryInTarget(REPORTING_SUBDIRECTORY, "file"))
            .exists()
            .isRegularFile();
    }

    @Test
    public void when_purge_is_called_then_existing_target_reporting_dir_is_merged_with_basedir_one() throws IOException {
        // given
        Path duringDir = folder.newFolder().toPath();
        feedDummyDirectory(duringDir, "during");
        Path afterDir = folder.newFolder().toPath();
        feedDummyDirectory(duringDir, "after");
        localStorage.duringExecution().toReporting().directory("during-dir").create(duringDir, f -> true, false);
        localStorage.duringExecution().toReporting().directory("after-dir").create(afterDir, f -> true, false);
        File target = folder.newFolder();

        // when
        localStorage.duringExecution().purge(target.getAbsolutePath());

        // then
        File reportingTargetDir =
            new File(target + File.separator + SMART_TESTING_TARGET_DIRECTORY_NAME, REPORTING_SUBDIRECTORY);
        File duringTargetDir = new File(reportingTargetDir, "during-dir");
        File afterTargetDir = new File(reportingTargetDir, "after-dir");
        softly.assertThat(reportingTargetDir.listFiles()).hasSize(2).contains(duringTargetDir, afterTargetDir);
        assertThatDirectoriesHaveSameContent(duringTargetDir.toPath(), duringDir);
        assertThatDirectoriesHaveSameContent(afterTargetDir.toPath(), afterDir);
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
        feedDummyDirectory(dirRoot, "file");
    }

    private void feedDummyDirectory(Path dirRoot, String prefixFileName) throws IOException {
        IntStream.range(1, 10).forEach(i -> createDummyFile(dirRoot, prefixFileName + i));
        Path subdirectory = Files.createDirectories(dirRoot.resolve(dirRoot.getFileName() + "subdirectory"));
        IntStream.range(1, 10).forEach(i -> createDummyFile(subdirectory, prefixFileName + i));
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

    private Path getSmartTestingSubdirectoryInTarget(String subdirectory, String fileName) {
        return Paths.get(folder.getRoot().getAbsolutePath(), "target", SMART_TESTING_TARGET_DIRECTORY_NAME, subdirectory,
            fileName);
    }
}
