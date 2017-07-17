package org.arquillian.smart.testing.hub.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.scm.Change;

public class LocalChangeStorage implements ChangeStorage {

    private static final Logger logger = Logger.getLogger(LocalChangeStorage.class);

    private static final String SMART_TESTING_SCM_CHANGES = ".smart-testing-scm-changes";

    private final String currentDirectory;

    // Used by SPI
    @SuppressWarnings("unused")
    public LocalChangeStorage() {
        this.currentDirectory = ".";
    }

    public LocalChangeStorage(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    @Override
    public void store(Collection<Change> changes) {
        try (BufferedWriter changesFile = Files.newBufferedWriter(Paths.get(currentDirectory, SMART_TESTING_SCM_CHANGES))) {
            changes.forEach(change -> {
                try {
                    changesFile.write(change.write());
                    changesFile.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot create " + SMART_TESTING_SCM_CHANGES + " file", e);
        }
    }

    @Override
    public Optional<Collection<Change>> read() {
        final Optional<Path> smartTestingScmChangesOptional =
            findFileInCurrentDirectoryOrParents(SMART_TESTING_SCM_CHANGES);

        if (smartTestingScmChangesOptional.isPresent()) {

            try (Stream<String> changes = Files.lines(smartTestingScmChangesOptional.get())) {
                return Optional.of(changes.map(Change::read).collect(Collectors.toList()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    @Override
    public void purgeAll() {
        final Optional<Path> smartTestingScmChangesOptional =
            findFileInCurrentDirectoryOrParents(SMART_TESTING_SCM_CHANGES);

        smartTestingScmChangesOptional.ifPresent(scmChanges -> {
            final File changesStore = new File(scmChanges.toUri());
            final boolean deleted = changesStore.delete();
            if (!deleted) {
                logger.warn("Unable to remove %s.", changesStore.getAbsolutePath());
            }
        });
    }

    private Optional<Path> findFileInCurrentDirectoryOrParents(String filename) {
        File currentFile = new File(currentDirectory, filename).getAbsoluteFile();
        if (currentFile.exists()) {
            return Optional.of(currentFile.toPath());
        }

        currentFile = currentFile.getParentFile().getParentFile();
        while (currentFile != null) {
            currentFile = new File(currentFile, filename);

            if (currentFile.exists()) {
                return Optional.of(currentFile.toPath());
            }

            currentFile = currentFile.getParentFile().getParentFile();
        }

        return Optional.empty();
    }
}
