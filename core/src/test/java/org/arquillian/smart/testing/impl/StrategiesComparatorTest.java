package org.arquillian.smart.testing.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class StrategiesComparatorTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { strategiesListOf("a", "c", "d", "e", "b"), strategiesListOf("a", "b", "c", "d", "e") },
            { strategiesListOf("d,e", "b,c,d", "a,b,d", "a", "a,c"), strategiesListOf("a,b,d", "a,c", "a", "b,c,d", "d,e") },
            { strategiesListOf("a,c,d", "a,c,e", "a,c,d,e", "a,b", "a,b,c,d,e"), strategiesListOf("a,b,c,d,e", "a,b", "a,c,d,e", "a,c,d", "a,c,e") },
            { strategiesListOf("b,c,d", "a", "b", "b,c,d,e", "c,d,e"), strategiesListOf("a", "b,c,d,e", "b,c,d", "b", "c,d,e") },
            { strategiesListOf("e", "c", "c,e", "a,d,e", "a,e"), strategiesListOf("a,d,e", "a,e", "c,e", "c", "e") },
            { strategiesListOf("b,d", "a,d", "d,e", "c", "b"), strategiesListOf("a,d", "b,d", "b", "c", "d,e") },
            { strategiesListOf("a,c,e", "a,c", "a,c,d", "a,b", "b,c"), strategiesListOf("a,b", "a,c,d", "a,c,e", "a,c", "b,c") },
            { strategiesListOf("c,e", "a,b", "a", "b"), strategiesListOf("a,b", "a", "b", "c,e") },
            { strategiesListOf("b,a", "e,a", "a,e", "a,b"), strategiesListOf("b,a", "a,b", "e,a", "a,e") },
            { strategiesListOf("a,b,d", "a", "a,d,b", "a,b"), strategiesListOf("a,b,d", "a,d,b", "a,b", "a") },
            { strategiesListOf("e", "d", "e,c", "e,d"), strategiesListOf("e,c", "e,d", "d", "e") },
            { strategiesListOf("c,d,e", "d,e,c", "d,c,e"), strategiesListOf("c,d,e", "d,e,c", "d,c,e") },
            { strategiesListOf("c,e", "e,d", "a"), strategiesListOf("a", "c,e", "e,d") },
            { strategiesListOf("b,c,d,e", "a"), strategiesListOf("a", "b,c,d,e") },
        });
    }

    @Parameterized.Parameter
    public List<List<String>> strategyCombinations;

    @Parameterized.Parameter(1)
    public List<List<String>> expectedOrder;

    private final StrategiesComparator strategiesComparator = new StrategiesComparator(Arrays.asList("a", "b", "c", "d", "e"));

    @Test
    public void should_order_strategies_combinations_based_on_their_priority() {
        // when
        strategyCombinations.sort(strategiesComparator);

        // then
        assertThat(strategyCombinations).isEqualTo(expectedOrder);
    }

    private static List<List<String>> strategiesListOf(String... strategies) {
        final List<List<String>> list = new ArrayList<>();
        Arrays.stream(strategies).forEach(s -> list.add(strategiesOf(s)));
        return list;
    }

    private static List<String> strategiesOf(String strategies) {
        return Arrays.asList(strategies.split("\\s*,\\s*"));
    }
}
