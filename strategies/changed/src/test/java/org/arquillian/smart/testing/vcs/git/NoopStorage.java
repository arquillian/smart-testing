package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;

public class NoopStorage implements ChangeStorage {
    @Override
    public void store(Collection<Change> changes, File projectDir) {

    }

    @Override
    public Optional<Collection<Change>> read(File projectDir) {
        return Optional.empty();
    }
}
