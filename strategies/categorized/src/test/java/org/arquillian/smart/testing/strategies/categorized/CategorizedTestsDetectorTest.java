package org.arquillian.smart.testing.strategies.categorized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.strategies.categorized.project.categories.FirstAndSecondCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.FirstCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.FirstCategory;
import org.arquillian.smart.testing.strategies.categorized.project.categories.NonCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.SecondCategory;
import org.arquillian.smart.testing.strategies.categorized.project.categories.ThirdCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.WithCategorizedMethodsClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.FastTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.FirstAndSecondTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.FirstTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.NonTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.ThirdTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.WithTaggedMethodsClass;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.strategies.categorized.CategorizedTestsDetector.CATEGORIZED;
import static org.arquillian.smart.testing.strategies.categorized.custom.assertions.TestSelectionCollectionAssert.assertThat;

public class CategorizedTestsDetectorTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final TemporaryFolder tmpFolderRule = new TemporaryFolder();

    private List<Class<?>> classesToProcess =
        Arrays.asList(ThirdCategorizedClass.class, FirstAndSecondCategorizedClass.class, FirstCategorizedClass.class,
            NonCategorizedClass.class, WithCategorizedMethodsClass.class);

    private List<Class<?>> taggedClassesToProcess =
        Arrays.asList(ThirdTaggedClass.class, FirstAndSecondTaggedClass.class, FirstTaggedClass.class,
            NonTaggedClass.class, FastTaggedClass.class, WithTaggedMethodsClass.class);

    private Configuration config;
    private CategorizedConfiguration categorizedConfig;

    @Before
    public void prepareConfig() {
        config = ConfigurationLoader.load(tmpFolderRule.getRoot());
        categorizedConfig =
            (CategorizedConfiguration) config.getStrategyConfiguration(CATEGORIZED);
        config.getStrategiesConfiguration().add(categorizedConfig);
    }

    @Test
    public void should_return_classes_and_methods_with_first_category_no_case_sensitive() {
        // given
        categorizedConfig.setCategories(new String[] {FirstCategory.class.getName().toLowerCase()});

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithCategorizedMethodsClass.class, "testWithFirstCategory");
        assertThat(testSelection)
            .containsTestClassSelectionsExactlyInAnyOrder(
                getTestSelection(FirstAndSecondCategorizedClass.class),
                getTestSelection(FirstCategorizedClass.class),
                testSelectionWithMethods);
    }

    @Test
    public void should_return_classes_and_methods_with_first_and_second_category_based_on_simple_class_name() {
        // given
        categorizedConfig.setCategories(
            new String[] {FirstCategory.class.getSimpleName(), SecondCategory.class.getSimpleName().toLowerCase()});

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithCategorizedMethodsClass.class, "testWithFirstCategory",
                "testWithSecondCategory");
        assertThat(testSelection)
            .containsTestClassSelectionsExactlyInAnyOrder(
                getTestSelection(FirstAndSecondCategorizedClass.class),
                getTestSelection(FirstCategorizedClass.class),
                testSelectionWithMethods);
    }

    @Test
    public void should_return_classes_and_methods_with_first_and_second_category_based_on_tags() {
        // given
        categorizedConfig.setCategories(
            new String[] {"first", "second"});

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(taggedClassesToProcess);

        // then
        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithTaggedMethodsClass.class, "testWithFirstTag", "testWithSecondTag");
        assertThat(testSelection)
            .containsTestClassSelectionsExactlyInAnyOrder(
                getTestSelection(FirstAndSecondTaggedClass.class),
                getTestSelection(FirstTaggedClass.class),
                testSelectionWithMethods);
    }

    @Test
    public void should_return_classes_and_methods_with_metatags() {

        // given
        categorizedConfig.setCategories(
            new String[] {"fast"});

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(taggedClassesToProcess);

        // then
        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithTaggedMethodsClass.class, "testWithFastTag");
        assertThat(testSelection)
            .containsTestClassSelectionsExactlyInAnyOrder(
                getTestSelection(FastTaggedClass.class),
                testSelectionWithMethods);

    }

    @Test
    public void should_return_class_and_methods_with_both_first_and_second_category_using_match_all() {
        // given
        categorizedConfig.setCategories(
            new String[] {FirstCategory.class.getSimpleName(), SecondCategory.class.getSimpleName().toLowerCase()});
        categorizedConfig.setMatchAll(true);

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        assertThat(testSelection)
            .containsTestClassSelectionsExactlyInAnyOrder(
                getTestSelection(FirstAndSecondCategorizedClass.class));
    }

    @Test
    public void should_return_class_and_methods_with_second_category_when_case_sensitivity_is_set() {
        // given
        categorizedConfig.setCategories(
            new String[] {FirstCategory.class.getSimpleName().toLowerCase(), SecondCategory.class.getSimpleName()});
        categorizedConfig.setCaseSensitive(true);

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithCategorizedMethodsClass.class, "testWithSecondCategory");
        assertThat(testSelection)
            .containsTestClassSelectionsExactlyInAnyOrder(
                getTestSelection(FirstAndSecondCategorizedClass.class),
                testSelectionWithMethods);
    }

    @Test
    public void should_return_all_classes_and_methods_with_any_category_when_no_category_is_set() {
        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithCategorizedMethodsClass.class, "testWithFirstCategory",
                "testWithSecondCategory", "testWithThirdCategory");
        assertThat(testSelection)
            .containsTestClassSelectionsExactlyInAnyOrder(
                getTestSelection(ThirdCategorizedClass.class),
                getTestSelection(FirstAndSecondCategorizedClass.class),
                getTestSelection(FirstCategorizedClass.class),
                testSelectionWithMethods);
    }

    @Test
    public void should_return_classes_and_methods_without_first_category_when_reversed_is_used() {
        // given
        categorizedConfig.setCategories(
            new String[] {FirstCategory.class.getSimpleName()});
        categorizedConfig.setReversed(true);

        // when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        // then
        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithCategorizedMethodsClass.class, "testWithSecondCategory",
                "testWithThirdCategory", "testWithoutCategory");
        assertThat(testSelection)
            .containsTestClassSelectionsExactlyInAnyOrder(
                getTestSelection(ThirdCategorizedClass.class),
                getTestSelection(NonCategorizedClass.class),
                testSelectionWithMethods);
    }

    private TestSelection getTestSelection(Class clazz) {
        return new TestSelection(clazz.getName(), CATEGORIZED);
    }

    private TestSelection getTestSelectionWithMethods(Class clazz, String... testMethods) {
        return new TestSelection(clazz.getName(), Arrays.asList(testMethods), CATEGORIZED);
    }
}
