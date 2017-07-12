package org.arquillian.smart.testing.spi;

import java.util.Collection;

public interface TestExecutionPlanner {

    // This will be already precalculated
    Collection<String> getTests();
    //

}
