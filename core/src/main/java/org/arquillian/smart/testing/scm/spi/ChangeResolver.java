package org.arquillian.smart.testing.scm.spi;

import java.io.File;
import java.util.Collection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.scm.Change;

public interface ChangeResolver extends AutoCloseable {

    Collection<Change> diff(File projectDir, Configuration configuration);

    /**
     * If projectDir doesn't have scm initialized, then {@link IllegalStateException} is thrown.
     *
     * @param projectDir A directory from which you are calculating git diff changes.
     * @param configuration A configuration used to configure Smart Testing.
     * @param strategy name of strategy for which you are calculating diff.
     *
     * @return Collection of entire diff in projectDir.
     */
    Collection<Change> diff(File projectDir, Configuration configuration, String strategy);

    boolean isApplicable(File projectDir);

}
