package org.arquillian.smart.testing.scm.spi;

import java.io.File;
import java.util.Collection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.scm.Change;

public interface ChangeResolver extends AutoCloseable {

    Collection<Change> diff(File projectDir, Configuration configuration);

    boolean isApplicable(File projectDir);

}
