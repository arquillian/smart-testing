package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class MyBusinessObjectTestCase {

    private MyBusinessObject myBusinessObject;

    @Test
    public void should_test_task() {
        myBusinessObject.doTask();
    }
}
