package org.arquillian.smart.testing.strategies.categorized;

import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.strategies.categorized.project.tags.FastTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.FirstAndSecondTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.FirstTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.NonTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.tags.ThirdTaggedClass;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TagsParserTest {

    @Test
    public void should_return_true_for_class_containing_no_tag_when_no_category_set() {

        // given
        CategorizedConfiguration categorizedConfig = prepareConfig();
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        TestSelection selection = tagsParser.getTestSelectionIfMatched(NonTaggedClass.class);

        // then
        Assertions.assertThat(selection).isNotEqualTo(TestSelection.NOT_MATCHED);
    }

    @Test
    public void should_return_true_for_class_containing_one_of_the_set_categories() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("first", "third");
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        TestSelection selection = tagsParser.getTestSelectionIfMatched(FirstAndSecondTaggedClass.class);

        // then
        Assertions.assertThat(selection).isNotEqualTo(TestSelection.NOT_MATCHED);
    }

    @Test
    public void should_return_true_for_class_containing_one_of_the_set_categories_as_metatag() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("fast", "third");
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        TestSelection selection = tagsParser.getTestSelectionIfMatched(FastTaggedClass.class);

        // then
        Assertions.assertThat(selection).isNotEqualTo(TestSelection.NOT_MATCHED);
    }

    @Test
    public void should_return_false_for_class_containing_none_of_the_set_categories() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("first", "second");
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        TestSelection selection = tagsParser.getTestSelectionIfMatched(ThirdTaggedClass.class);

        // then
        Assertions.assertThat(selection).isEqualTo(TestSelection.NOT_MATCHED);
    }

    @Test
    public void should_return_false_for_class_not_containing_any_of_the_set_tags_when_case_sensitive() {
        // given
        CategorizedConfiguration categorizedConfig = prepareConfig("First",
            "Second");
        categorizedConfig.setCaseSensitive(true);
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        TestSelection selection = tagsParser.getTestSelectionIfMatched(FirstAndSecondTaggedClass.class);

        // then
        Assertions.assertThat(selection).isEqualTo(TestSelection.NOT_MATCHED);
    }

    private CategorizedConfiguration prepareConfig(String... categories) {
        CategorizedConfiguration categorizedConfig = new CategorizedConfiguration();
        categorizedConfig.setCategories(categories);
        categorizedConfig.setExcludedCategories(new String[0]);
        return categorizedConfig;
    }
}
