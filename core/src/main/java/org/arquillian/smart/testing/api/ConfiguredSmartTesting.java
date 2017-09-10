package org.arquillian.smart.testing.api;

import java.io.File;

/**
 * Represents configured Smart-Testing allowing to set additional parameters
 */
public interface ConfiguredSmartTesting extends TestStrategyApplier {

    /**
     * Sets the given project directory to Smart-Testing logic as the root directory of a project the tool should be
     * applied on
     *
     * @param projectDirectory
     *     A path to the root directory of a project the Smart-Testing tool should be applied on
     *
     * @return An instance of {@link TestStrategyApplier}
     */
    TestStrategyApplier in(String projectDirectory);

    /**
     * Sets the given project directory to Smart-Testing logic as the root directory of a project the tool should be
     * applied on
     *
     * @param projectDirectory
     *     A file representing a root directory of a project the Smart-Testing tool should be applied on
     *
     * @return An instance of {@link TestStrategyApplier}
     */
    TestStrategyApplier in(File projectDirectory);
}
