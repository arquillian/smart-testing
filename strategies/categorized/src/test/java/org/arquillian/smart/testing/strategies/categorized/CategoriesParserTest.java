package org.arquillian.smart.testing.strategies.categorized;

import org.arquillian.smart.testing.strategies.categorized.project.FirstAndSecondCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.FirstCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.NonCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.ThirdCategorizedClass;
import org.arquillian.smart.testing.strategies.categorized.project.categories.FirstCategory;
import org.arquillian.smart.testing.strategies.categorized.project.categories.SecondCategory;
import org.arquillian.smart.testing.strategies.categorized.project.categories.ThirdCategory;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CategoriesParserTest {

    @Test
    public void should_return_true_for_class_containing_category_when_no_category_set() {
        // given
        CategorizedConfiguration categorizedConfig = prepareConfig();
        CategoriesParser categoriesParser = new CategoriesParser(categorizedConfig);

        // when
        boolean hasCorrectCategories = categoriesParser.hasCorrectCategories(FirstCategorizedClass.class);

        // then
        Assertions.assertThat(hasCorrectCategories).isTrue();
    }

    @Test
    public void should_return_false_for_class_containing_no_category_when_no_category_set() {
        // given
        CategorizedConfiguration categorizedConfig = prepareConfig();
        CategoriesParser categoriesParser = new CategoriesParser(categorizedConfig);

        // when
        boolean hasCorrectCategories = categoriesParser.hasCorrectCategories(NonCategorizedClass.class);

        // then
        Assertions.assertThat(hasCorrectCategories).isFalse();
    }

    @Test
    public void should_return_true_for_class_containing_one_of_the_set_categories() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig(FirstCategory.class.getSimpleName().toLowerCase(), ThirdCategory.class.getName());
        CategoriesParser categoriesParser = new CategoriesParser(categorizedConfig);

        // when
        boolean hasCorrectCategories = categoriesParser.hasCorrectCategories(FirstAndSecondCategorizedClass.class);

        // then
        Assertions.assertThat(hasCorrectCategories).isTrue();
    }

    @Test
    public void should_return_false_for_class_containing_none_of_the_set_categories() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig(FirstCategory.class.getSimpleName(), SecondCategory.class.getName());
        CategoriesParser categoriesParser = new CategoriesParser(categorizedConfig);

        // when
        boolean hasCorrectCategories = categoriesParser.hasCorrectCategories(ThirdCategorizedClass.class);

        // then
        Assertions.assertThat(hasCorrectCategories).isFalse();
    }

    @Test
    public void should_return_true_for_class_containing_all_of_the_set_categories_when_matching_all() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig(FirstCategory.class.getSimpleName().toLowerCase(), SecondCategory.class.getName());
        categorizedConfig.setMatchAll(true);
        CategoriesParser categoriesParser = new CategoriesParser(categorizedConfig);

        // when
        boolean hasCorrectCategories = categoriesParser.hasCorrectCategories(FirstAndSecondCategorizedClass.class);

        // then
        Assertions.assertThat(hasCorrectCategories).isTrue();
    }

    @Test
    public void should_return_false_for_class_not_containing_all_of_the_set_categories_when_matching_all() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig(FirstCategory.class.getSimpleName(), SecondCategory.class.getSimpleName());
        categorizedConfig.setMatchAll(true);
        CategoriesParser categoriesParser = new CategoriesParser(categorizedConfig);

        // when
        boolean hasCorrectCategories = categoriesParser.hasCorrectCategories(FirstCategorizedClass.class);

        // then
        Assertions.assertThat(hasCorrectCategories).isFalse();
    }

    @Test
    public void should_return_false_for_class_not_containing_any_of_the_set_categories_when_case_sensitive() {
        // given
        CategorizedConfiguration categorizedConfig = prepareConfig(FirstCategory.class.getSimpleName().toLowerCase(),
            SecondCategory.class.getName().toLowerCase());
        categorizedConfig.setCaseSensitive(true);
        CategoriesParser categoriesParser = new CategoriesParser(categorizedConfig);

        // when
        boolean hasCorrectCategories = categoriesParser.hasCorrectCategories(FirstAndSecondCategorizedClass.class);

        // then
        Assertions.assertThat(hasCorrectCategories).isFalse();
    }

    @Test
    public void should_return_true_for_class_containing_all_of_the_set_categories_when_matching_all_and_case_sensitive() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig(FirstCategory.class.getSimpleName(), SecondCategory.class.getName());
        categorizedConfig.setMatchAll(true);
        categorizedConfig.setCaseSensitive(true);
        CategoriesParser categoriesParser = new CategoriesParser(categorizedConfig);

        // when
        boolean hasCorrectCategories = categoriesParser.hasCorrectCategories(FirstAndSecondCategorizedClass.class);

        // then
        Assertions.assertThat(hasCorrectCategories).isTrue();
    }

    @Test
    public void should_return_false_for_class_not_containing_all_of_the_set_categories_when_matching_all_and_case_sensitive() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig(FirstCategory.class.getSimpleName(), SecondCategory.class.getName().toLowerCase());
        categorizedConfig.setMatchAll(true);
        categorizedConfig.setCaseSensitive(true);
        CategoriesParser categoriesParser = new CategoriesParser(categorizedConfig);

        // when
        boolean hasCorrectCategories = categoriesParser.hasCorrectCategories(FirstAndSecondCategorizedClass.class);

        // then
        Assertions.assertThat(hasCorrectCategories).isFalse();
    }

    private CategorizedConfiguration prepareConfig(String... categories) {
        CategorizedConfiguration categorizedConfig = new CategorizedConfiguration();
        categorizedConfig.setCategories(categories);
        return categorizedConfig;
    }
}
