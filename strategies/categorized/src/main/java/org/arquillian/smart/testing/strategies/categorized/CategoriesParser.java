package org.arquillian.smart.testing.strategies.categorized;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;

class CategoriesParser extends AbstractParser {

    private final Logger logger = Log.getLogger();

    CategoriesParser(CategorizedConfiguration strategyConfig) {
        super(strategyConfig);
    }

    @Override
    protected List<String> findCategories(Class<?> clazz) {
        return Arrays.stream(clazz.getAnnotations())
            .filter(this::isJUnit4Category)
            .flatMap(this::retrieveCategoriesFromAnnotation)
            .map(this::changeIfNonCaseSensitive)
            .collect(Collectors.toList());
    }

    @Override
    protected boolean isSpecified(String category, List<String> specifiedCategories) {
        if (!specifiedCategories.contains(category)) {
            if (category.contains(".")) {
                String categoryClassName = category.substring(category.lastIndexOf(".") + 1);
                return specifiedCategories.contains(categoryClassName);
            }
            return false;
        }
        return true;
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
