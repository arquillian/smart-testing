package org.arquillian.smart.testing.configuration;

import java.util.Collection;

import static java.lang.Math.min;

public class StringSimilarityCalculator {

    /**
     * Finds closest matching string from targets comparing to passed sample using
     * <a href="https://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein distance</a> method.
     *
     * @param sample string for which the closest matching one will be found
     * @param targets targets to compare against
     * @return
     */
    public String findClosestMatch(String sample, Collection<String> targets) {
        int distance = sample.length();
        String closestMatch = "";
        for (final String target : targets) {
            final int currentDistance = calculateLevenshteinDistance(sample, target);
            if (currentDistance < distance) {
                distance = currentDistance;
                closestMatch = target;
            }
        }
        return closestMatch;
    }

    private int calculateLevenshteinDistance(String s, String t) {
        final int[] previousDistances = new int[t.length() + 1];
        final int[] currentDistances = new int[t.length() + 1];
        for (int i = 0; i < previousDistances.length; i++) {
            previousDistances[i] = i;
        }

        final char[] source = s.toCharArray();
        final char[] target = t.toCharArray();

        for (int i = 0; i < source.length; i++) {
            currentDistances[0] = i + 1;
            for (int j = 0; j < target.length; j++) {
                int cost = 1;
                if (source[i] == target[j]) {
                    cost = 0;
                }
                final int costOfInsertion = currentDistances[j] + 1;
                final int costOfRemoval = previousDistances[j + 1] + 1;
                final int costOfSubstitution = previousDistances[j] + cost;
                currentDistances[j + 1] = min(costOfInsertion, min(costOfRemoval, costOfSubstitution));
            }
            System.arraycopy(currentDistances, 0, previousDistances, 0, currentDistances.length);
        }
        return currentDistances[target.length];
    }


}
