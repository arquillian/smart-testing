package org.arquillian.smart.testing.strategies.categorized;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;

import static org.arquillian.smart.testing.strategies.categorized.CategorizedTestsDetector.CATEGORIZED;

public abstract class AbstractParser {

    private final CategorizedConfiguration strategyConfig;
    private final List<String> specifiedCategories;

    AbstractParser(CategorizedConfiguration strategyConfig) {
        this.strategyConfig = strategyConfig;
        specifiedCategories = Arrays.stream(strategyConfig.getCategories())
            .map(this::changeIfNonCaseSensitive)
            .collect(Collectors.toList());
    }

    TestSelection getTestSelectionIfMatched(Class<?> clazz){
        List<String> classCategories = findCategories(clazz.getAnnotations());
        if (strategyConfig.isMethods() && classCategories.isEmpty()) {
            return getSelectionWithMethods(clazz);
        } else {
            if (containsCorrectCategoriesMatchingReversed(classCategories)) {
                return new TestSelection(clazz.getName(), CATEGORIZED);
            }
        }
        return TestSelection.NOT_MATCHED;
    }

    private TestSelection getSelectionWithMethods(Class<?> clazz){
        List<Method> testMethods = Arrays.stream(clazz.getDeclaredMethods())
            .filter(this::isTestMethod)
            .collect(Collectors.toList());

        List<String> selectedMethods = testMethods.stream()
            .filter(method -> containsCorrectCategoriesMatchingReversed(findCategories(method.getAnnotations())))
            .map(Method::getName)
            .collect(Collectors.toList());

        if (!selectedMethods.isEmpty()) {
            if (selectedMethods.size() != testMethods.size()) {
                return new TestSelection(clazz.getName(), selectedMethods, CATEGORIZED);
            } else {
                return new TestSelection(clazz.getName(), CATEGORIZED);
            }
        }
        return TestSelection.NOT_MATCHED;
    }

    private boolean containsCorrectCategoriesMatchingReversed(List<String> presentCategories) {
        if (strategyConfig.isReversed()) {
            return !containsCorrectCategories(presentCategories);
        }
        return containsCorrectCategories(presentCategories);
    }

    private boolean containsCorrectCategories(List<String> presentCategories) {
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

    protected abstract List<String> findCategories(Annotation[] annotations);
    protected abstract boolean isSpecified(String category, List<String> specifiedCategories);
    protected abstract boolean isTestMethod(Method method);

}
