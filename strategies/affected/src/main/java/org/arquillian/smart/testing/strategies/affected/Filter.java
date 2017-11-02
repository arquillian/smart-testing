package org.arquillian.smart.testing.strategies.affected;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Filter {

    private final List<String> inclusions = new ArrayList<>();
    private final List<String> exclusions = new ArrayList<>();

    Filter(List<String> inclusions, List<String> exclusions) {

        if (inclusions != null && !inclusions.isEmpty()) {
            this.inclusions.addAll(parse(inclusions));
        }

        if (exclusions != null && !exclusions.isEmpty()) {
            this.exclusions.addAll(parse(exclusions));
        }

    }

    private List<String> parse(List<String> expressions) {
        return expressions.stream()
            .map(preProcessedToken -> {
                if (preProcessedToken.endsWith("*")) {
                    return preProcessedToken.substring(0, preProcessedToken.length() - 1);
                } else {
                    return preProcessedToken;
                }
            }).collect(Collectors.toList());
    }

    boolean shouldBeIncluded(String element) {

        if (inclusions.isEmpty() && exclusions.isEmpty()) {
            return true;
        }

        final boolean excluded = exclusions.stream().anyMatch(element::startsWith);

        return !excluded && (inclusions.isEmpty() || inclusions.stream().anyMatch(element::startsWith));
    }

}
