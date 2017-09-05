package org.arquillian.smart.testing.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class StrategiesComparator implements Comparator<Collection<String>> {
    private final Map<String, Double> priorities;
    private final List<String> strategiesByPriority;

    StrategiesComparator(List<String> strategiesByPriority) {
        this.strategiesByPriority = strategiesByPriority;
        this.priorities = getPriorities();
    }

    @Override
    public int compare(Collection<String> list1, Collection<String> list2) {
        final double sum1 = list1.stream().mapToDouble(priorities::get).sum();
        final double sum2 = list2.stream().mapToDouble(priorities::get).sum();
        return Double.compare(sum2, sum1);
    }

    private Map<String, Double> getPriorities() {
        return strategiesByPriority.stream()
            .collect(Collectors.toMap(Function.identity(), this::getPriority));
    }

    private Double getPriority(String strategy) {
        return Math.pow(10, strategiesByPriority.size() - strategiesByPriority.indexOf(strategy) - 1);
    }
}
