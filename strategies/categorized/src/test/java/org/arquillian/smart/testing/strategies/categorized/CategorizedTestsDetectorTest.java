package org.arquillian.smart.testing.strategies.categorized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.strategies.categorized.project.categories.FirstCategory;
import org.arquillian.smart.testing.strategies.categorized.project.categories.FirstCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.SecondCategory;
import org.arquillian.smart.testing.strategies.categorized.project.categories.FirstAndSecondCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.ThirdCategory;
import org.arquillian.smart.testing.strategies.categorized.project.categories.ThirdCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.NonCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.WithCategorizedMethodsClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.Included;
import org.arquillian.smart.testing.strategies.categorized.project.categories.Excluded;
import org.arquillian.smart.testing.strategies.categorized.project.categories.WithExcludedMethodClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.WithIncludedAndExcludedCategoriesOnMethodsClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.FastTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.FirstTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.FirstAndSecondTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.NonTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.ThirdTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.WithTaggedMethodsClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.WithExcludedTagClass;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.strategies.categorized.CategorizedTestsDetector.CATEGORIZED;
import static org.arquillian.smart.testing.custom.assertions.TestSelectionCollectionAssert.assertThat;

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
    public void should_return_all_classes_and_methods_with_any_category_when_no_category_is_set() {
        //given
        categorizedConfig.setCategories(new String[0]);

        //when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(classesToProcess);

        //then
        assertThat(testSelection).containsTestClassSelectionsExactlyInAnyOrder(
            classesToProcess.stream().map(this::getTestSelection).toArray(TestSelection[]::new));
    }

    @Test
    public void should_exclude_classes_with_matching_category_when_no_included_category_is_set() {
        //given
        categorizedConfig.setCategories(new String[0]);
        categorizedConfig.setExcludedCategories(new String[] {SecondCategory.class.getSimpleName()});

        //when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(Arrays.asList(FirstCategorizedClass.class,
                FirstAndSecondCategorizedClass.class));
        //then
        assertThat(testSelection).containsTestClassSelectionsExactlyInAnyOrder(getTestSelection(FirstCategorizedClass.class));
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
    public void should_return_methods_without_excluded_category() {
        //given
        categorizedConfig.setCategories(new String[] {Included.class.getSimpleName()});
        categorizedConfig.setExcludedCategories(new String[] {Excluded.class.getSimpleName()});

        //when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(
                Collections.singleton(WithExcludedMethodClass.class));

        //then
        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithExcludedMethodClass.class, "testWithoutExcludedCategory");

        assertThat(testSelection).containsTestClassSelectionsExactlyInAnyOrder(testSelectionWithMethods);
    }

    @Test
    public void should_return_methods_without_excluded_tag() {
        //given
        categorizedConfig.setCategories(new String[] {"included"});
        categorizedConfig.setExcludedCategories(new String[] {"excluded"});

        //when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(
                Collections.singleton(WithExcludedTagClass.class));

        //then
        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithExcludedTagClass.class, "includedMethod");

        assertThat(testSelection).containsTestClassSelectionsExactlyInAnyOrder(testSelectionWithMethods);
    }

    @Test
    public void should_return_class_when_no_methods_are_matched_to_be_excluded() {
        //given
        categorizedConfig.setCategories(new String[] {FirstCategory.class.getSimpleName()});
        categorizedConfig.setExcludedCategories(new String[] {Excluded.class.getSimpleName()});

        //when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(
                Collections.singleton(FirstCategorizedClass.class));

        assertThat(testSelection).containsTestClassSelectionsExactlyInAnyOrder(
            getTestSelection(FirstCategorizedClass.class));
    }

    @Test
    public void should_exclude_test_class_when_excluding_single_category_of_many() {
        //given
        categorizedConfig.setCategories(new String[] {FirstCategory.class.getSimpleName()});
        categorizedConfig.setExcludedCategories(new String[] {SecondCategory.class.getSimpleName()});

        //when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(
                Collections.singleton(FirstAndSecondCategorizedClass.class));

        Assert.assertTrue(testSelection.isEmpty());
    }

    @Test
    public void should_exclude_methods_when_matched_and_class_is_not_annotated() {
        //given
        categorizedConfig.setCategories(new String[] {FirstCategory.class.getSimpleName()});
        categorizedConfig.setExcludedCategories(
            new String[] {SecondCategory.class.getSimpleName(), ThirdCategory.class.getSimpleName()});

        //when
        Collection<TestSelection> testSelection =
            new CategorizedTestsDetector(config).selectTestsFromClasses(
                Collections.singleton(WithIncludedAndExcludedCategoriesOnMethodsClass.class));

        TestSelection testSelectionWithMethods =
            getTestSelectionWithMethods(WithIncludedAndExcludedCategoriesOnMethodsClass.class, "testWithFirstCategory");

        assertThat(testSelection).containsTestClassSelectionsExactlyInAnyOrder(testSelectionWithMethods);
    }

    private TestSelection getTestSelection(Class clazz) {
        return new TestSelection(clazz.getName(), CATEGORIZED);
    }

    private TestSelection getTestSelectionWithMethods(Class clazz, String... testMethods) {
        return new TestSelection(clazz.getName(), Arrays.asList(testMethods), CATEGORIZED);
    }
}
