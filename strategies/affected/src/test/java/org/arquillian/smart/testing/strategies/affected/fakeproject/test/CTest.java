package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.fakeproject.main.C;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Test ignored because it is used internally")
public class CTest {

    private C c;

    @Test
    public void should_test_task(){
        c.doTask();
    }

}
