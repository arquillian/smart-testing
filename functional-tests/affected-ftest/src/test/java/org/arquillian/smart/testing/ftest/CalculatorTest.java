package org.arquillian.smart.testing.ftest;

import org.junit.Assert;
import org.junit.Test;

public class CalculatorTest {

    @Test
    public void should_add_numbers() {
        Assert.assertEquals(6, Calculator.add(4, 2));
    }
}
