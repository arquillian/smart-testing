package org.arquillian.smart.testing.strategies.categorized;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractParser {

    private final CategorizedConfiguration strategyConfig;
    private final List<String> specifiedCategories;

    AbstractParser(CategorizedConfiguration strategyConfig) {
        this.strategyConfig = strategyConfig;
        specifiedCategories = Arrays.stream(strategyConfig.getCategories())
            .map(this::changeIfNonCaseSensitive)
            .collect(Collectors.toList());
    }

    boolean hasCorrectCategories(Class<?> clazz) {
        final List<String> presentCategories = findCategories(clazz);
        if (presentCategories.isEmpty()) {
            return false;
        }

        final List<String> intersection = presentCategories.stream()
            .filter(category -> this.isSpecified(category, specifiedCategories))
            .collect(Collectors.toList());

        if (strategyConfig.isMatchAll() || specifiedCategories.isEmpty()) {
            return intersection.size() == specifiedCategories.size();
        } else {
            return !intersection.isEmpty();
        }
    }

    protected String changeIfNonCaseSensitive(String category) {
        return strategyConfig.isCaseSensitive() ? category : category.toLowerCase();
    }

    protected abstract List<String> findCategories(Class<?> clazz);
    protected abstract boolean isSpecified(String category, List<String> specifiedCategories);

}
