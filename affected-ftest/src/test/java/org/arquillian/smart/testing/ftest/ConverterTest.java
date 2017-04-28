package org.arquillian.smart.testing.ftest;

import org.junit.Assert;
import org.junit.Test;

public class ConverterTest {

    @Test
    public void should_convert_from_celsius_to_kelvin() {
        Assert.assertEquals(283.15, Converter.fromCelsiusToKelvin(10), 0);
    }
}
