package org.arquillian.smart.testing;

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSelectionTest {
    
    private static final String NEW = "new";
    private static final String CHANGED = "changed";
    private static final String AFFECTED = "affected";
    private static final String CLASS_NAME_1 = "smart.testing.Class1";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_merge_test_selection_with_different_strategy_for_same_class() {
        // given
        final TestSelection testSelection = new TestSelection(CLASS_NAME_1, NEW);
        
        // when
        final TestSelection mergedTestSelection = testSelection.merge(new TestSelection(CLASS_NAME_1, CHANGED));
        
        // then
        assertThat(mergedTestSelection.getAppliedStrategies()).containsExactly(NEW, CHANGED);
        assertThat(mergedTestSelection.getClassName()).isEqualTo(CLASS_NAME_1);
    }

    @Test
    public void should_merge_test_selection_with_different_strategies_for_same_class_name() {
        // given
        final TestSelection testSelection = new TestSelection(CLASS_NAME_1, NEW, AFFECTED);

        // when
        final TestSelection mergedTestSelection = testSelection.merge(new TestSelection(CLASS_NAME_1, CHANGED));

        // then
        assertThat(mergedTestSelection.getAppliedStrategies()).containsExactly(NEW, AFFECTED, CHANGED);
        assertThat(mergedTestSelection.getClassName()).isEqualTo(CLASS_NAME_1);
    }

    @Test
    public void should_not_merge_test_selection_with_different_class_name() {
        // given
        final TestSelection testSelection = new TestSelection(getPath("DummyClassWithDefaultPackageName.java"), NEW);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot merge two test selections with different locations (DummyClassWithDefaultPackageName != dummy.cls.DummyClassWithPackageName)");

        // when
        testSelection.merge(new TestSelection(getPath("DummyClassWithPackageName.java"), CHANGED));
        
        // then exception should be thrown
    }

    @Test
    public void should_merge_test_selection_with_different_strategies_for_same_class() {
        // given
        final Path path = getPath("DummyClassWithDefaultPackageName.java");
        final TestSelection testSelection = new TestSelection(path, AFFECTED);
        // when
        final TestSelection mergedTestSelection = testSelection.merge(new TestSelection(path, NEW));

        // then
        assertThat(mergedTestSelection.getAppliedStrategies()).containsExactly(AFFECTED, NEW);
    }

    @Test
    public void should_merge_test_methods_selection_when_in_both_are_specified() {
        // given
        final String className = "TestClass.java";
        final TestSelection testSelection = new TestSelection(className, Arrays.asList("firstTestMethod"), AFFECTED);

        // when
        final TestSelection mergedTestSelection =
            testSelection.merge(new TestSelection(className, Arrays.asList("secondTestMethod"), NEW));

        // then
        assertThat(mergedTestSelection.getTestMethodNames()).containsExactly("firstTestMethod", "secondTestMethod");
    }

    @Test
    public void should_return_merged_test_selection_without_methods_when_the_other_one_does_not_contain_any() {
        // given
        final String className = "TestClass.java";
        final TestSelection testSelection = new TestSelection(className, Arrays.asList("firstTestMethod"), AFFECTED);

        // when
        final TestSelection mergedTestSelection = testSelection.merge(new TestSelection(className, NEW));

        // then
        assertThat(mergedTestSelection.getTestMethodNames()).isEmpty();
    }

    private Path getPath(String fileName) {
        return Paths.get(Thread.currentThread().getContextClassLoader().getResource(
            fileName).getFile());
    }

}
