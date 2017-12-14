package org.arquillian.smart.testing.strategies.categorized;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;

class CategoriesParser {

    private final Logger logger = Log.getLogger();
    private final CategorizedConfiguration strategyConfig;
    private final List<String> specifiedCategories;

    CategoriesParser(CategorizedConfiguration strategyConfig) {
        this.strategyConfig = strategyConfig;
        specifiedCategories = Arrays.stream(strategyConfig.getCategories())
            .map(this::changeIfNonCaseSensitive)
            .collect(Collectors.toList());
    }

    boolean hasCorrectCategories(Class<?> clazz) {
        final List<String> presentCategories = getPresentCategories(clazz);
        if (presentCategories.isEmpty()) {
            return false;
        }

        final List<String> intersection = presentCategories.stream()
            .filter(category -> isSpecified(category, specifiedCategories))
            .collect(Collectors.toList());

        if (strategyConfig.isMatchAll() || specifiedCategories.isEmpty()) {
            return intersection.size() == specifiedCategories.size();
        } else {
            return !intersection.isEmpty();
        }
    }

    private List<String> getPresentCategories(Class<?> clazz) {
        return Arrays.stream(clazz.getAnnotations())
            .filter(this::isJUnit4Category)
            .flatMap(this::retrieveCategoriesFromAnnotation)
            .map(this::changeIfNonCaseSensitive)
            .collect(Collectors.toList());
    }

    private boolean isSpecified(String category, List<String> specifiedCategories) {
        if (!specifiedCategories.contains(category)) {
            if (category.contains(".")) {
                String categoryClassName = category.substring(category.lastIndexOf(".") + 1);
                return specifiedCategories.contains(categoryClassName);
            }
            return false;
        }
        return true;
    }

    private String changeIfNonCaseSensitive(String category) {
        return strategyConfig.isCaseSensitive() ? category : category.toLowerCase();
    }

    private Stream<String> retrieveCategoriesFromAnnotation(Annotation categoryAnnotation) {
        try {
            Method valueMethod = categoryAnnotation.getClass().getMethod("value");
            return Arrays.stream((Class[]) valueMethod.invoke(categoryAnnotation))
                .map(Class::getName)
                .collect(Collectors.toList())
                .stream();
        } catch (Exception e) {
            logger.warn("Something wrong happened when the annotation [%s] was being parsed: %s", categoryAnnotation,
                e.getMessage());
        }
        return Stream.empty();
    }

    private boolean isJUnit4Category(Annotation annotation) {
        String fgn = annotation.annotationType().getName();
        return fgn.equals("org.junit.experimental.categories.Category");
    }
}
