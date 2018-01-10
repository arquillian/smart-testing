package org.arquillian.smart.testing.strategies.categorized;

import org.arquillian.smart.testing.strategies.categorized.project.FastTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.FirstAndSecondTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.FirstTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.NonTaggedClass;
import org.arquillian.smart.testing.strategies.categorized.project.ThirdTaggedClass;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TagsParserTest {

    @Test
    public void should_return_true_for_class_containing_tag_when_no_category_set() {
        // given
        CategorizedConfiguration categorizedConfig = prepareConfig();
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagsParser.hasCorrectTags(FirstTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isTrue();
    }

    @Test
    public void should_return_false_for_class_containing_no_tag_when_no_category_set() {
        // given
        CategorizedConfiguration categorizedConfig = prepareConfig();
        TagsParser tagParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagParser.hasCorrectTags(NonTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isFalse();
    }

    @Test
    public void should_return_true_for_class_containing_one_of_the_set_categories() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("first", "third");
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagsParser.hasCorrectTags(FirstAndSecondTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isTrue();
    }

    @Test
    public void should_return_true_for_class_containing_one_of_the_set_categories_as_metatag() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("fast", "third");
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagsParser.hasCorrectTags(FastTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isTrue();
    }

    @Test
    public void should_return_false_for_class_containing_none_of_the_set_categories() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("first", "second");
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagsParser.hasCorrectTags(ThirdTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isFalse();
    }

    @Test
    public void should_return_true_for_class_containing_all_of_the_set_categories_when_matching_all() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("first", "second");
        categorizedConfig.setMatchAll(true);
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagsParser.hasCorrectTags(FirstAndSecondTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isTrue();
    }

    @Test
    public void should_return_false_for_class_not_containing_all_of_the_set_categories_when_matching_all() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("first", "second");
        categorizedConfig.setMatchAll(true);
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagsParser.hasCorrectTags(FirstTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isFalse();
    }

    @Test
    public void should_return_false_for_class_not_containing_any_of_the_set_tags_when_case_sensitive() {
        // given
        CategorizedConfiguration categorizedConfig = prepareConfig("First",
            "Second");
        categorizedConfig.setCaseSensitive(true);
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagsParser.hasCorrectTags(FirstAndSecondTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isFalse();
    }

    @Test
    public void should_return_true_for_class_containing_all_of_the_set_tags_when_matching_all_and_case_sensitive() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("first", "second");
        categorizedConfig.setMatchAll(true);
        categorizedConfig.setCaseSensitive(true);
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagsParser.hasCorrectTags(FirstAndSecondTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isTrue();
    }

    @Test
    public void should_return_false_for_class_not_containing_all_of_the_set_tags_when_matching_all_and_case_sensitive() {
        // given
        CategorizedConfiguration categorizedConfig =
            prepareConfig("first", "Second");
        categorizedConfig.setMatchAll(true);
        categorizedConfig.setCaseSensitive(true);
        TagsParser tagsParser = new TagsParser(categorizedConfig);

        // when
        boolean hasCorrectTags = tagsParser.hasCorrectTags(FirstAndSecondTaggedClass.class);

        // then
        Assertions.assertThat(hasCorrectTags).isFalse();
    }

    private CategorizedConfiguration prepareConfig(String... categories) {
        CategorizedConfiguration categorizedConfig = new CategorizedConfiguration();
        categorizedConfig.setCategories(categories);
        return categorizedConfig;
    }

}
