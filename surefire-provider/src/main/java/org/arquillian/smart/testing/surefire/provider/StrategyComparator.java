package org.arquillian.smart.testing.surefire.provider;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;

class StrategyComparator implements Comparator<TestSelection> {
    private final Map<String, Integer> priorities;
    private final List<String> strategies;

    StrategyComparator(List<String> strategies) {
        this.strategies = strategies;
        this.priorities = priority();
    }

    private Map<String, Integer> priority() {
        return strategies.stream()
            .collect(Collectors.toMap(Function.identity(), s -> (strategies.size() - strategies.indexOf(s)) * 100));
    }

    @Override
    public int compare(TestSelection testSelection1, TestSelection testSelection2) {
        final int sum1 = testSelection1.getTypes().stream().mapToInt(priorities::get).sum();
        final int sum2 = testSelection2.getTypes().stream().mapToInt(priorities::get).sum();

        return Integer.compare(sum1, sum2);
    }
}
