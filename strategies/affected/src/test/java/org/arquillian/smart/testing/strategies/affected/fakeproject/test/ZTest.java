package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.Tests;
import org.junit.Ignore;
import org.junit.Test;

// Test ignored because it is used internally
@Ignore
//tag::docs[]
@Tests(packages = "org.arquillian.smart.testing.strategies.affected.fakeproject.main.superbiz.*")
public class ZTest {
//end::docs[]
    @Test
    public void black_box() {
        System.out.println("Black Box Testing");
    }

}
