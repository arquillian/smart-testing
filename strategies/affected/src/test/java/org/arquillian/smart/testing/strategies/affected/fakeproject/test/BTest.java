package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.fakeproject.main.B;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Test ignored because it is used internally")
public class BTest {

    private B b;

    @Test
    public void should_test_task(){
        b.doTask();
    }

}
