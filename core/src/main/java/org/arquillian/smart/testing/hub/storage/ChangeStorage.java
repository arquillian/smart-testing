package org.arquillian.smart.testing.hub.storage;

import java.util.Collection;
import java.util.Optional;
import org.arquillian.smart.testing.scm.Change;

public interface ChangeStorage {

    void store(Collection<Change> changes);

    Optional<Collection<Change>> read();


}
