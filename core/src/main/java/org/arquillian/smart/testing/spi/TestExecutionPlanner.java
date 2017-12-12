package org.arquillian.smart.testing.spi;

import java.util.Collection;
import org.arquillian.smart.testing.TestSelection;

public interface TestExecutionPlanner {

    //tag::documentation[]
    Collection<TestSelection> selectTestsFromNames(Iterable<String> testsToRun);

    Collection<TestSelection> selectTestsFromClasses(Iterable<Class<?>> testsToRun);
    //end::documentation[]

    String getName();

}
