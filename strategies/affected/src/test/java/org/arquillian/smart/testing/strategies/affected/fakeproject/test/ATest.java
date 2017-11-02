package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.fakeproject.main.A;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Test ignored because it is used internally")
public class ATest {

    private A a;

    @Test
    public void should_test_task(){
        a.doTask();
    }

}
