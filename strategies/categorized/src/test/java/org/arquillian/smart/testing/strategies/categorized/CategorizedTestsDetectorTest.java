package org.arquillian.smart.testing.strategies.categorized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.SmartTesting;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.strategies.categorized.project.FirstAndSecondCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.FirstCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.NonCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.ThirdCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.FirstCategory;
import org.arquillian.smart.testing.strategies.categorized.project.categories.SecondCategory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class CategorizedTestsDetectorTest {

    @Rule
    public final TemporaryFolder tmpFolderRule = new TemporaryFolder();

    private List<Class<?>> classesToProcess =
        Arrays.asList(ThirdCategorizedClass.class, FirstAndSecondCategorizedClass.class, FirstCategorizedClass.class,
            NonCategorizedClass.class);

    private Configuration config;
    private CategorizedConfiguration categorizedConfig;

    @Before
    public void prepareConfig() {
        config = ConfigurationLoader.load(tmpFolderRule.getRoot());
        categorizedConfig =
            (CategorizedConfiguration) config.getStrategyConfiguration(CategorizedTestsDetector.CATEGORIZED);
        config.getStrategiesConfiguration().add(categorizedConfig);
    }

    @Test
    public void should_return_classes_with_first_category_no_case_sensitive() {
        // given
        categorizedConfig.setCategories(new String[] {FirstCategory.class.getName().toLowerCase()});

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        assertThat(SmartTesting.getClasses(new HashSet<>(testSelection)))
            .containsExactlyInAnyOrder(FirstAndSecondCategorizedClass.class, FirstCategorizedClass.class);
    }

    @Test
    public void should_return_classes_with_first_and_second_category_based_on_simple_class_name() {
        // given
        categorizedConfig.setCategories(
            new String[] {FirstCategory.class.getSimpleName(), SecondCategory.class.getSimpleName().toLowerCase()});

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        assertThat(SmartTesting.getClasses(new HashSet<>(testSelection)))
            .containsExactlyInAnyOrder(FirstAndSecondCategorizedClass.class, FirstCategorizedClass.class);
    }

    @Test
    public void should_return_class_with_both_first_and_second_category_using_match_all() {
        // given
        categorizedConfig.setCategories(
            new String[] {FirstCategory.class.getSimpleName(), SecondCategory.class.getSimpleName().toLowerCase()});
        categorizedConfig.setMatchAll(true);

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        assertThat(SmartTesting.getClasses(new HashSet<>(testSelection)))
            .containsExactly(FirstAndSecondCategorizedClass.class);
    }

    @Test
    public void should_return_class_with_second_category_when_case_sensitivity_is_set() {
        // given
        categorizedConfig.setCategories(
            new String[] {FirstCategory.class.getSimpleName().toLowerCase(), SecondCategory.class.getSimpleName()});
        categorizedConfig.setCaseSensitive(true);

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        assertThat(SmartTesting.getClasses(new HashSet<>(testSelection)))
            .containsExactly(FirstAndSecondCategorizedClass.class);
    }

    @Test
    public void should_return_all_classes_with_any_category_when_no_category_is_set() {
        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        assertThat(SmartTesting.getClasses(new HashSet<>(testSelection)))
            .containsExactlyInAnyOrder(ThirdCategorizedClass.class, FirstAndSecondCategorizedClass.class,
                FirstCategorizedClass.class);
    }

    @Test
    public void should_return_classes_without_first_category_when_reversed_is_used() {
        // given
        categorizedConfig.setCategories(
            new String[] {FirstCategory.class.getSimpleName()});
        categorizedConfig.setReversed(true);

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        assertThat(SmartTesting.getClasses(new HashSet<>(testSelection)))
            .containsExactlyInAnyOrder(ThirdCategorizedClass.class, NonCategorizedClass.class);
    }
}
