package org.arquillian.smart.testing.hub.storage;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import org.arquillian.smart.testing.scm.Change;

public interface ChangeStorage {

    void store(Collection<Change> changes, File projectDir);

    Optional<Collection<Change>> read(File projectDir);


}
