package org.arquillian.smart.testing.strategies.categorized;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

public class CategorizedTestsDetector implements TestExecutionPlanner {

    static final String CATEGORIZED = "categorized";

    private final CategorizedConfiguration strategyConfig;
    private final CategoriesParser categoriesParser;
    private final TagsParser tagsParser;

    public CategorizedTestsDetector(Configuration configuration) {
        strategyConfig = (CategorizedConfiguration) configuration.getStrategyConfiguration(CATEGORIZED);
        categoriesParser = new CategoriesParser(strategyConfig);
        tagsParser = new TagsParser(strategyConfig);
    }

    @Override
    public Collection<TestSelection> selectTestsFromNames(Iterable<String> testsToRun) {
        List<Class<?>> classes = StreamSupport.stream(testsToRun.spliterator(), false)
            .map(name -> {
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("It is not possible to use categorized strategy in this environment. "
                        + "The classes you want to run are not on the classpath when Smart Testing is invoked, so they cannot be loaded.");
                }
            }).collect(Collectors.toList());
        return selectTestsFromClasses(classes);
    }

    @Override
    public Collection<TestSelection> selectTestsFromClasses(Iterable<Class<?>> testsToRun) {
        return StreamSupport.stream(testsToRun.spliterator(), false)
            .filter(this::hasCorrectCategoriesMatchingReversed)
            .map(clazz -> new TestSelection(clazz.getName(), CATEGORIZED))
            .collect(Collectors.toList());
    }

    private boolean hasCorrectCategoriesMatchingReversed(Class<?> clazz) {
        if (strategyConfig.isReversed()) {
            return !categoriesParser.hasCorrectCategories(clazz) && !tagsParser.hasCorrectCategories(clazz);
        }
        return categoriesParser.hasCorrectCategories(clazz) || tagsParser.hasCorrectCategories(clazz);
    }

    @Override
    public String getName() {
        return CATEGORIZED;
    }
}
