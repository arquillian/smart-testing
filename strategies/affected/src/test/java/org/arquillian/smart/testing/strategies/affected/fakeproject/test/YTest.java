package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.Tests;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.superbiz.Alone;
import org.junit.Ignore;
import org.junit.Test;

// Test ignored because it is used internally
@Ignore
@Tests(packagesOf = Alone.class)
public class YTest {

    @Test
    public void black_box() {
        System.out.println("Black Box Testing");
    }

}
