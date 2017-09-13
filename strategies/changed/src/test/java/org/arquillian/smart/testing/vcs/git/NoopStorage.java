package org.arquillian.smart.testing.vcs.git;

import java.util.Collection;
import java.util.Optional;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;

public class NoopStorage implements ChangeStorage {
    @Override
    public void store(Collection<Change> changes) {

    }

    @Override
    public Optional<Collection<Change>> read() {
        return Optional.empty();
    }
}
