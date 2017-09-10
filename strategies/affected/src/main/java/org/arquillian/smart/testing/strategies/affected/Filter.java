package org.arquillian.smart.testing.strategies.affected;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Filter {

    private final List<String> inclusions = new ArrayList<>();
    private final List<String> exclusions = new ArrayList<>();

    /**
     * Constructor to set inclusions and exlusions to filter.
     * @param inclusions in CSV format
     * @param exclusions in CSV format
     */
    public Filter(String inclusions, String exclusions) {

        if (inclusions != null && !inclusions.isEmpty()) {
            this.inclusions.addAll(parse(inclusions));
        }

        if (exclusions != null && !exclusions.isEmpty()) {
            this.exclusions.addAll(parse(exclusions));
        }

    }

    private List<String> parse(String expression) {

        final List<String> tokens = new ArrayList<>();
        final StringTokenizer stringTokenizer = new StringTokenizer(expression, ",");
        while(stringTokenizer.hasMoreTokens()) {
            final String preProcessedToken = stringTokenizer.nextToken().trim();

            if (preProcessedToken.endsWith("*")) {
                tokens.add(preProcessedToken.substring(0, preProcessedToken.length() - 1));
            } else {
                tokens.add(preProcessedToken);
            }

        }

        return tokens;
    }

    boolean shouldBeIncluded(String element) {

        if (inclusions.isEmpty() && exclusions.isEmpty()) {
            return true;
        }

        final boolean excluded = exclusions.stream().anyMatch(element::startsWith);

        return !excluded && (inclusions.isEmpty() || inclusions.stream().anyMatch(element::startsWith));
    }

}
