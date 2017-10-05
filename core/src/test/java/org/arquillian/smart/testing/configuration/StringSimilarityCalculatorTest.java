package org.arquillian.smart.testing.configuration;

import java.util.Arrays;
import org.arquillian.smart.testing.configuration.StringSimilarityCalculator;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringSimilarityCalculatorTest {

    @Test
    public void should_find_exact_match() throws Exception {
        // given
        final String sample = "sample";

        // when
        final String closestMatch =
            new StringSimilarityCalculator().findClosestMatch(sample, Arrays.asList("smaple", "sapmle", "", "sample"));

        // then
        assertThat(closestMatch).isEqualTo(sample);
    }

    @Test
    public void should_find_match_for_misspelled_strategy() throws Exception {
        // given
        final String misspelled = "cahnged";
        final String expectedWord = "changed";

        // when
        final String closestMatch =
            new StringSimilarityCalculator().findClosestMatch(misspelled, Arrays.asList("failed", "affected", "changed", "new"));

        // then
        assertThat(closestMatch).isEqualTo(expectedWord);
    }


}
