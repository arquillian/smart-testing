package org.arquillian.smart.testing.spi;

public interface TestExecutionPlanner {

    Iterable<Class<?>> getTests();
}
