package org.arquillian.smart.testing.api;

import java.util.Set;
import org.arquillian.smart.testing.TestSelection;

/**
 * Takes care of applying specified strategies and mode on the given classes
 */
public interface TestStrategyApplier {

    /**
     * Applies specified strategies and mode on the list of class names
     *
     * @param testsToRun A list of class names the specified strategies and mode should be applied on
     * @return Optimized list of {@link TestSelection}s based on the specified strategies and mode
     */
    Set<TestSelection> applyOnNames(Iterable<String> testsToRun);

    /**
     * Applies specified strategies and mode on the list of classes
     *
     * @param testsToRun A list of classes the specified strategies and mode should be applied on
     * @return Optimized list of {@link TestSelection}s based on the specified strategies and mode
     */
    Set<TestSelection> applyOnClasses(Iterable<Class<?>> testsToRun);
}
