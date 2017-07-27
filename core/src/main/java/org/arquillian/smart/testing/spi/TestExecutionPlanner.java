package org.arquillian.smart.testing.spi;

import java.util.Collection;
import org.arquillian.smart.testing.TestSelection;

public interface TestExecutionPlanner {

    Collection<TestSelection> getTests();

    String getName();

}
