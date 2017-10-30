package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.scm.Change;

import static org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles.getScmChangesFileName;

public class LocalChangeStorage implements ChangeStorage {

    private static final Logger LOGGER = Log.getLogger();

    @Override
    public void store(Collection<Change> changes, File projectDir) {
        StringBuilder fileContent = new StringBuilder();
        changes.forEach(change -> fileContent.append(change.write()).append(System.lineSeparator()));

        LocalStorageFileAction scmChangesFile =
            new LocalStorage(projectDir)
                .duringExecution()
                .temporary()
                .file(getScmChangesFileName());
        try {
            scmChangesFile.create(fileContent.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Cannot create " + scmChangesFile.getPath() + " file", e);
        }
    }

    @Override
    public Optional<Collection<Change>> read(File projectDir) {
        final Optional<Path> smartTestingScmChangesOptional =
            findFileInDirectoryOrParents(projectDir.getAbsoluteFile(), getScmChangesFileName());

        if (smartTestingScmChangesOptional.isPresent()) {

            final Path localScmChanges = smartTestingScmChangesOptional.get();
            try (Stream<String> changes = Files.lines(localScmChanges)) {
                return Optional.of(changes.map(Change::read).collect(Collectors.toList()));
            } catch (IOException e) {
                LOGGER.warn("Unable to read changes from [%s]. Reason: %s", localScmChanges, e.getMessage());
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    private Optional<Path> findFileInDirectoryOrParents(File directory, String fileName) {
        if (directory == null || !directory.exists()){
            return Optional.empty();
        }

        File currentFile = new LocalStorage(directory).duringExecution().temporary().file(fileName).getFile();
        if (currentFile.exists()) {
            return Optional.of(currentFile.toPath());
        }

        return findFileInDirectoryOrParents(directory.getParentFile(), fileName);
    }
}
