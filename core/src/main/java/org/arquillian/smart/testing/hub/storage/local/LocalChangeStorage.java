package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;

public class LocalChangeStorage implements ChangeStorage {

    private static final String SMART_TESTING_SCM_CHANGES = "smart-testing-scm-changes";

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
        StringBuilder fileContent = new StringBuilder();
        changes.forEach(change -> fileContent.append(change.write()).append(System.lineSeparator()));

        SubDirectoryFileAction scmChangesFile =
            new LocalStorage(currentDirectory)
                .execution()
                .file(SMART_TESTING_SCM_CHANGES);
        try {
            scmChangesFile.create(fileContent.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Cannot create " + scmChangesFile.getPath() + " file", e);
        }
    }

    @Override
    public Optional<Collection<Change>> read() {
        final Optional<Path> smartTestingScmChangesOptional =
            findFileInDirectoryOrParents(new File(currentDirectory).getAbsoluteFile(), SMART_TESTING_SCM_CHANGES);

        if (smartTestingScmChangesOptional.isPresent()) {

            try (Stream<String> changes = Files.lines(smartTestingScmChangesOptional.get())) {
                return Optional.of(changes.map(Change::read).collect(Collectors.toList()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    private Optional<Path> findFileInDirectoryOrParents(File directory, String fileName) {
        if (directory == null || !directory.exists()){
            return Optional.empty();
        }

        File currentFile = new LocalStorage(directory).execution().file(fileName).getFile();
        if (currentFile.exists()) {
            return Optional.of(currentFile.toPath());
        }

        return findFileInDirectoryOrParents(directory.getParentFile(), fileName);
    }
}
