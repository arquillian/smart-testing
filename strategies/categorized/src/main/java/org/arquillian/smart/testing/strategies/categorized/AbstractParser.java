package org.arquillian.smart.testing.strategies.categorized;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;

import java.util.stream.Stream;
import org.arquillian.smart.testing.TestSelection;

import static org.arquillian.smart.testing.strategies.categorized.CategorizedTestsDetector.CATEGORIZED;

public abstract class AbstractParser {

    private final CategorizedConfiguration strategyConfig;
    private final List<String> specifiedCategories;
    private final List<String> excludedCategories;

    AbstractParser(CategorizedConfiguration strategyConfig) {
        this.strategyConfig = strategyConfig;
        specifiedCategories = Arrays.stream(strategyConfig.getCategories())
            .map(this::changeIfNonCaseSensitive)
            .collect(Collectors.toList());
        excludedCategories = Arrays.stream(strategyConfig.getExcludedCategories())
            .map(this::changeIfNonCaseSensitive)
            .collect(Collectors.toList());
    }

    TestSelection getTestSelectionIfMatched(Class<?> clazz) {
        if (strategyConfig.isMethods()) {
            return getSelectionWithMethods(clazz);
        }
        return shouldBeIncluded(findCategories(clazz.getAnnotations())) ? new TestSelection(clazz.getName(), CATEGORIZED)
            : TestSelection.NOT_MATCHED;
    }

    private boolean matchesCategories(Collection<String> presentCategories, Collection<String> categories) {
        if (presentCategories.isEmpty()) {
            return false;
        }

        final List<String> intersection = presentCategories.stream()
            .filter(category -> isSpecified(category, new ArrayList<>(categories)))
            .collect(Collectors.toList());
        return !intersection.isEmpty();
    }

    private boolean shouldBeIncluded(Collection<String> presentCategories) {
        if (specifiedCategories.isEmpty()) {
            return true;
        }
        return matchesCategories(presentCategories, specifiedCategories);
    }

    private boolean areCategoriesIncluded(Collection<String> presentCategories) {
        return shouldBeIncluded(presentCategories) && !matchesCategories(presentCategories, excludedCategories);
    }

    private TestSelection getSelectionWithMethods(Class<?> clazz) {
        List<String> classLevelCategories = findCategories(clazz.getAnnotations());
        List<CategorizedMethod> categorizedMethods = getTestMethods(clazz)
            .stream()
            .map(method -> new CategorizedMethod(method, classLevelCategories))
            .collect(Collectors.toList());
        List<String> applicableMethodNames = categorizedMethods.stream()
            .filter(method -> areCategoriesIncluded(method.getCategories()))
            .map(CategorizedMethod::getName)
            .collect(Collectors.toList());
        if (applicableMethodNames.size() == getTestMethods(clazz).size()) {
            return new TestSelection(clazz.getName(), CATEGORIZED);
        }
        if (!applicableMethodNames.isEmpty()) {
            return new TestSelection(clazz.getName(), applicableMethodNames, CATEGORIZED);
        }
        return TestSelection.NOT_MATCHED;
    }

    private List<Method> getTestMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
            .filter(this::isTestMethod)
            .collect(Collectors.toList());
    }

    protected String changeIfNonCaseSensitive(String category) {
        return strategyConfig.isCaseSensitive() ? category : category.toLowerCase();
    }

    protected abstract List<String> findCategories(Annotation[] annotations);

    protected abstract boolean isSpecified(String category, List<String> specifiedCategories);

    protected abstract boolean isTestMethod(Method method);

    private class CategorizedMethod {

        private final String name;
        private final Set<String> categories;

        CategorizedMethod(Method method, Collection<String> classLevelCategories) {
            this.name = method.getName();
            this.categories = Stream.concat(classLevelCategories.stream(), findCategories(method.getAnnotations()).stream())
                .collect(Collectors.toSet());
        }

        Set<String> getCategories() {
            return categories;
        }

        String getName() {
            return name;
        }
    }
}
