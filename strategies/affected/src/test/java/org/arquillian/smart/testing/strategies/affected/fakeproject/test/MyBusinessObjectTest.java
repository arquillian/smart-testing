package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject;
import org.junit.Ignore;
import org.junit.Test;

// Test ignored because it is a test that is used to in tests and not to be run as real test by test runner
@Ignore
public class MyBusinessObjectTest {

    private MyBusinessObject myBusinessObject;

    @Test
    public void should_test_task() {
        myBusinessObject.doTask();
    }

}
