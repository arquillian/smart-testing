package org.arquillian.smart.testing.ftest;

import org.junit.Assert;
import org.junit.Test;

public class FirstJUnitTest {

    @Test
    public void should_print_text_of_first_test() {
        System.out.println("This is first junit test");
        Assert.assertTrue(true);
    }
}
