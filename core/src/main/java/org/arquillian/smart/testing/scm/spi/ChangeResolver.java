package org.arquillian.smart.testing.scm.spi;

import java.util.Set;
import org.arquillian.smart.testing.scm.Change;

public interface ChangeResolver extends AutoCloseable {

    Set<Change> diff();

    boolean isApplicable();

}
