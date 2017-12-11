package org.arquillian.smart.testing.spi;

import java.util.Collection;
import org.arquillian.smart.testing.TestSelection;

public interface TestExecutionPlanner {

    Collection<TestSelection> selectTestsFromNames(Iterable<String> testsToRun);

    Collection<TestSelection> selectTestsFromClasses(Iterable<Class<?>> testsToRun);

    String getName();

}
