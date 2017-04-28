package org.arquillian.smart.testing.ftest;

import org.junit.Assert;
import org.junit.Test;

public class SecondTest {

    @Test
    public void should_print_text_of_second_test() {
        System.out.println(">>>>> This is second junit test");
        Assert.assertTrue(true);
    }
}
