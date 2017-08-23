package org.arquillian.smart.testing.surefire.provider;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class StrategiesComparator implements Comparator<Collection<String>> {
    private final Map<String, Double> priorities;
    private final List<String> strategies;

    StrategiesComparator(List<String> strategies) {
        this.strategies = strategies;
        this.priorities = getPriorities();
    }

    @Override
    public int compare(Collection<String> list1, Collection<String> list2) {
        final double sum1 = list1.stream().mapToDouble(priorities::get).sum();
        final double sum2 = list2.stream().mapToDouble(priorities::get).sum();

        return Double.compare(sum2, sum1);
    }

    private Map<String, Double> getPriorities() {
        return strategies.stream()
            .collect(Collectors.toMap(Function.identity(), this::getPriority));
    }

    private Double getPriority(String strategy) {
        return Math.pow(10, strategies.size() - strategies.indexOf(strategy) - 1);
    }
}
