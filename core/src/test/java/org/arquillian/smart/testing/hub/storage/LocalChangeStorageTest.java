package org.arquillian.smart.testing.hub.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.arquillian.smart.testing.hub.storage.local.LocalChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalChangeStorageTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_read_changes_when_current_dir_is_equal_to_root_dir() {
        // given
        final File rootDir = temporaryFolder.getRoot();
        LocalChangeStorage localChangeStorage = new LocalChangeStorage();

        final List<Change> changes =
            Collections.singletonList(new Change(rootDir.toPath().resolve("mychange.txt"), ChangeType.ADD));

        // when
        localChangeStorage.store(changes, rootDir);
        final Optional<Collection<Change>> readChangesOptional = localChangeStorage.read(rootDir);
        
        // then
        assertThat(readChangesOptional).contains(changes);
    }

    @Test
    public void should_read_changes_when_current_dir_is_a_subdir_of_root_dir() throws IOException {
        // given
        final File rootDir = temporaryFolder.getRoot();
        final File subDir = temporaryFolder.newFolder();
        final List<Change> changes =
            Collections.singletonList(new Change(rootDir.toPath().resolve("mychange.txt"), ChangeType.ADD));

        final LocalChangeStorage mvnExtensionLocalChangeStorage = new LocalChangeStorage();
        final LocalChangeStorage surefireExecutionLocalChangeStorage = new LocalChangeStorage();

        // when
        mvnExtensionLocalChangeStorage.store(changes, rootDir);

        final Optional<Collection<Change>> readChangesOptional = surefireExecutionLocalChangeStorage.read(subDir);

        // then
        assertThat(readChangesOptional).contains(changes);
    }

    @Test
    public void should_read_changes_when_current_dir_is_a_deepsubdir_of_root_dir() throws IOException {
        // given
        final File rootDir = temporaryFolder.getRoot();
        final Path deepDirectory = Files.createDirectories(Paths.get(rootDir.getAbsolutePath(), "level1", "level2", "level3"));

        final LocalChangeStorage mvnExtensionLocalChangeStorage = new LocalChangeStorage();
        final LocalChangeStorage surefireExecutionLocalChangeStorage = new LocalChangeStorage();

        final List<Change> changes =
            Collections.singletonList(new Change(rootDir.toPath().resolve("mychange.txt"), ChangeType.ADD));

        // when
        mvnExtensionLocalChangeStorage.store(changes, rootDir);

        final Optional<Collection<Change>> readChangesOptional = surefireExecutionLocalChangeStorage.read(deepDirectory.toFile());

        // then
        assertThat(readChangesOptional).contains(changes);
    }

    @Test
    public void should_return_empty_if_file_not_found() {
        // given
        final File rootDir = temporaryFolder.getRoot();
        LocalChangeStorage localChangeStorage = new LocalChangeStorage();

        // when
        final Optional<Collection<Change>> readChangesOptional = localChangeStorage.read(rootDir);

        // then
        assertThat(readChangesOptional).isEmpty();

    }

}
