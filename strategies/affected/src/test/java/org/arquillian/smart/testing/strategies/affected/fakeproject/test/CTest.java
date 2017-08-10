package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.fakeproject.main.C;
import org.junit.Ignore;
import org.junit.Test;

// Test ignored because it is a test that is used to in tests and not to be run as real test by test runner
@Ignore
public class CTest {

    private C c;

    @Test
    public void should_test_task(){
        c.doTask();
    }

}
