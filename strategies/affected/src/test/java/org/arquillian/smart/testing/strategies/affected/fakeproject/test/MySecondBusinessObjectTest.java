package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyControllerObject;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Test ignored because it is used internally")
public class MySecondBusinessObjectTest {

    private MyBusinessObject myBusinessObject;
    private MyControllerObject myControllerObject;

    @Test
    public void should_test_task() {
        myControllerObject.doTask();
        myBusinessObject.doTask();
    }

}
