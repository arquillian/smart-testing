package org.arquillian.smart.testing.scm.spi;

import java.io.File;
import java.util.Collection;
import org.arquillian.smart.testing.scm.Change;

public interface ChangeResolver extends AutoCloseable {

    Collection<Change> diff(File projectDir);

    boolean isApplicable(File projectDir);

}
