package org.arquillian.smart.testing.strategies.affected;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FilterTest {

    @Test
    public void should_include_when_no_filter_is_set() {
        // given
        Filter filter = new Filter(null, null);

        // when
        final boolean isIncluded = filter.shouldBeIncluded("org.mypackage.mycontroller.Class");

        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void should_exclude_elements_if_matches() {
        // given
        Filter filter = new Filter(null, Arrays.asList("org.mypackage.*", "org.otherpackage.Class"));

        // when
        final boolean isIncluded = filter.shouldBeIncluded("org.mypackage.mycontroller.Class");

        // then
        assertThat(isIncluded).isFalse();
    }

    @Test
    public void should_not_exclude_if_not_matches() {
        // given
        Filter filter = new Filter(null, Arrays.asList("org.mypackage.*", "org.otherpackage.Class"));

        // when
        final boolean isIncluded = filter.shouldBeIncluded("org.otherpackage.mycontroller.Class");

        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void should_exclude_specific_classes() {
        // given
        Filter filter = new Filter(null, Arrays.asList("org.mypackage.*", "org.otherpackage.Class"));

        // when
        final boolean isIncluded = filter.shouldBeIncluded("org.otherpackage.Class");

        // then
        assertThat(isIncluded).isFalse();
    }

    @Test
    public void should_include_if_matches() {
        // given
        Filter filter = new Filter(Arrays.asList("org.mypackage.*", "org.otherpackage.Class"), null);

        // when
        final boolean isIncluded = filter.shouldBeIncluded("org.mypackage.Class");

        // then
        assertThat(isIncluded).isTrue();
    }

    @Test
    public void should_not_be_included_if_not_matches() {
        // given
        Filter filter = new Filter(Arrays.asList("org.mypackage.*", "org.otherpackage.Class"), null);

        // when
        final boolean isIncluded = filter.shouldBeIncluded("org.mystrangepackage.Class");

        // then
        assertThat(isIncluded).isFalse();
    }

    @Test
    public void should_take_precedence_exclusions_than_inclusions() {
        // given
        Filter filter = new Filter(Arrays.asList("org.mypackage.*", "org.otherpackage.Class"),
            Arrays.asList("org.mypackage.*", "org.otherpackage.Class"));

        // when
        final boolean isIncluded = filter.shouldBeIncluded("org.mypackage.Class");

        // then
        assertThat(isIncluded).isFalse();
    }

    @Test
    public void should_exclude_java_jdk_classes() {
        // given
        Filter filter = new Filter(Collections.singletonList(""), Collections.singletonList("java*"));

        // when
        final boolean isIncluded = filter.shouldBeIncluded("java.util.List");

        // then
        assertThat(isIncluded).isFalse();
    }

}
