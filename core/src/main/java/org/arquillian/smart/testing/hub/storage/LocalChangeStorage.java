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

    private static final String SMART_TESTING_PLAN = ".smart-testing-plan";

    @Override
    public void store(Collection<Change> changes) {
        try (BufferedWriter changesFile = Files.newBufferedWriter(getStoragePath())) {
            changes.forEach(change -> {
                try {
                    changesFile.write(change.write());
                    changesFile.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot create " + SMART_TESTING_PLAN + " file", e);
        }
    }

    @Override
    public Optional<Collection<Change>> read() {
        try (Stream<String> changes = Files.lines(getStoragePath())) {
            return Optional.of(changes.map(Change::read).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void purgeAll() {
        final File changesStore = new File(getStoragePath().toUri());
        final boolean deleted = changesStore.delete();
        if (!deleted) {
            logger.warn("Unable to remove %s.", changesStore.getAbsolutePath());
        }
    }

    private Path getStoragePath() {
        // TODO we have to make it configurable
        // Paths.get(".") - not working
        // "java.io.tmpdir" is overwritten to target mvn folder while in surefire so we cannot rely on system property
        final Path path = Paths.get("/tmp", SMART_TESTING_PLAN);
        return path;
    }
}
