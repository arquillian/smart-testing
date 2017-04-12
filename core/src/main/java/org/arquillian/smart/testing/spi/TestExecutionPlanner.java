package org.arquillian.smart.testing.spi;

import java.util.Collection;

public interface TestExecutionPlanner {

    Collection<String> getTests();
}
