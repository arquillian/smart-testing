package org.arquillian.smart.testing.surefire.provider;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilsTest {

    @Test
    public void should_be_false_for_null() {
        // when
        final boolean isNumeric = StringUtils.isNumeric(null);

        // then
        assertThat(isNumeric).isFalse();
    }

    @Test
    public void should_be_false_for_float_string() {
        // when
        final boolean isNumeric = StringUtils.isNumeric("1.23");

        // then
        assertThat(isNumeric).isFalse();
    }

    @Test
    public void should_be_true_for_integer_string() {
        // when
        final boolean isNumeric = StringUtils.isNumeric("10");

        // then
        assertThat(isNumeric).isTrue();
    }

    @Test
    public void should_be_false_for_invalid_string() {
        // when
        final boolean isNumeric = StringUtils.isNumeric("a123");

        // then
        assertThat(isNumeric).isFalse();
    }
}
