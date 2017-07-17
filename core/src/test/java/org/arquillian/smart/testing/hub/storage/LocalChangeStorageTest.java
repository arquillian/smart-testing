package org.arquillian.smart.testing.hub.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalChangeStorageTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_read_changes_when_current_dir_is_equal_to_root_dir() {

        // given
        final String rootDir = temporaryFolder.getRoot().getAbsolutePath();
        LocalChangeStorage localChangeStorage = new LocalChangeStorage();
        LocalChangeStorage.CURRENT_DIR = rootDir;

        final List<Change> changes = Arrays.asList(new Change(Paths.get(rootDir, "mychange.txt"), ChangeType.ADD));

        // when
        localChangeStorage.store(changes);
        final Optional<Collection<Change>> readChangesOptional = localChangeStorage.read();
        
        // then
        assertThat(readChangesOptional).contains(changes);
    }

    @Test
    public void should_read_changes_when_current_dir_is_a_subdir_of_root_dir() throws IOException {

        // given
        final String rootDir = temporaryFolder.getRoot().getAbsolutePath();
        final String subDir = temporaryFolder.newFolder().getAbsolutePath();

        LocalChangeStorage localChangeStorage = new LocalChangeStorage();
        LocalChangeStorage.CURRENT_DIR = rootDir;

        final List<Change> changes = Arrays.asList(new Change(Paths.get(rootDir, "mychange.txt"), ChangeType.ADD));

        // when
        localChangeStorage.store(changes);
        LocalChangeStorage.CURRENT_DIR = subDir;

        final Optional<Collection<Change>> readChangesOptional = localChangeStorage.read();

        // then
        assertThat(readChangesOptional).contains(changes);
    }

    @Test
    public void should_read_changes_when_current_dir_is_a_deepsubdir_of_root_dir() throws IOException {

        // given
        final String rootDir = temporaryFolder.getRoot().getAbsolutePath();
        final Path deepDirectory = Files.createDirectories(Paths.get(rootDir, "level1", "level2", "level3"));

        LocalChangeStorage localChangeStorage = new LocalChangeStorage();
        LocalChangeStorage.CURRENT_DIR = rootDir;

        final List<Change> changes = Arrays.asList(new Change(Paths.get(rootDir, "mychange.txt"), ChangeType.ADD));

        // when
        localChangeStorage.store(changes);
        LocalChangeStorage.CURRENT_DIR = deepDirectory.toString();

        final Optional<Collection<Change>> readChangesOptional = localChangeStorage.read();

        // then
        assertThat(readChangesOptional).contains(changes);
    }

    @Test
    public void should_return_empty_if_file_not_found() {

        // given
        final String rootDir = temporaryFolder.getRoot().getAbsolutePath();
        LocalChangeStorage localChangeStorage = new LocalChangeStorage();
        LocalChangeStorage.CURRENT_DIR = rootDir;

        // when
        final Optional<Collection<Change>> readChangesOptional = localChangeStorage.read();

        // then
        assertThat(readChangesOptional).isEmpty();

    }

}
