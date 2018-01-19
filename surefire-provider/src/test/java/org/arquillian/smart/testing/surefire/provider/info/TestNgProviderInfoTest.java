package org.arquillian.smart.testing.surefire.provider.info;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNgProviderInfoTest {

    @Test
    public void should_be_false_for_null() {
        // given
        final TestNgProviderInfo providerInfo = new TestNgProviderInfo();

        // when
        final boolean isNumeric = providerInfo.isNumeric(null);

        // then
        assertThat(isNumeric).isFalse();
    }

    @Test
    public void should_be_false_for_float_string() {
        // given
        final TestNgProviderInfo providerInfo = new TestNgProviderInfo();

        // when
        final boolean isNumeric = providerInfo.isNumeric("1.23");

        // then
        assertThat(isNumeric).isFalse();
    }

    @Test
    public void should_be_true_for_integer_string() {
        // given
        final TestNgProviderInfo providerInfo = new TestNgProviderInfo();

        // when
        final boolean isNumeric = providerInfo.isNumeric("10");

        // then
        assertThat(isNumeric).isTrue();
    }

    @Test
    public void should_be_false_for_invalid_string() {
        // given
        final TestNgProviderInfo providerInfo = new TestNgProviderInfo();

        // when
        final boolean isNumeric = providerInfo.isNumeric("a123");

        // then
        assertThat(isNumeric).isFalse();
    }
}
