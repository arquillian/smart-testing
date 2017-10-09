package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.Tests;
import org.junit.Ignore;
import org.junit.Test;

// Test ignored because it is a test that is used to in tests and not to be run as real test by test runner
@Ignore
@Tests(packages = "org.arquillian.smart.testing.strategies.affected.fakeproject.main.superbiz.*")
public class ZTest {

    @Test
    public void black_box() {
        System.out.println("Black Box Testing");
    }

}
