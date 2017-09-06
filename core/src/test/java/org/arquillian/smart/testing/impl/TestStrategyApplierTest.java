package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.TestSelection;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestStrategyApplierTest {

    @Test
    public void should_return_merged_test_selections_if_test_selection_has_same_class() {
        // given
        final TestSelection testSelectionNew = new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "new");
        final TestSelection testSelectionChanged =
            new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "changed");

        final Set<Class<?>> classes =
            new LinkedHashSet<>(Collections.singletonList(TestExecutionPlannerLoaderTest.class));
        TestExecutionPlannerLoader testExecutionPlannerLoader = prepareLoader(classes);

        final TestStrategyApplierImpl testStrategyApplier =
            new TestStrategyApplierImpl(Configuration.load(), testExecutionPlannerLoader, new File("."));

        // when
        final Collection<TestSelection> testSelections =
            testStrategyApplier.filterMergeAndOrderTestSelection(asList(testSelectionNew, testSelectionChanged),
                asList("new", "changed"));

        // then
        Assertions.assertThat(testSelections)
            .hasSize(1)
            .flatExtracting("types").containsExactly("new", "changed");
    }

    @Test
    public void should_not_return_merged_test_selections_if_test_selection_has_different_class() {
        // given
        final TestSelection testSelectionNew = new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "new");
        final TestSelection testSelectionChanged =
            new TestSelection(TestStrategyApplierTest.class.getName(), "changed");

        final Set<Class<?>> classes =
            new LinkedHashSet<>(asList(TestExecutionPlannerLoaderTest.class, TestStrategyApplierTest.class));
        TestExecutionPlannerLoader testExecutionPlannerLoader = prepareLoader(classes);

        final TestStrategyApplierImpl testStrategyApplier =
            new TestStrategyApplierImpl(Configuration.load(), testExecutionPlannerLoader, new File("."));

        // when
        final Collection<TestSelection> testSelections =
            testStrategyApplier.filterMergeAndOrderTestSelection(asList(testSelectionNew, testSelectionChanged),
                asList("new", "changed"));

        // then
        Assertions.assertThat(testSelections)
            .hasSize(2)
            .flatExtracting("types").containsExactly("new", "changed");
    }

    private TestExecutionPlannerLoader prepareLoader(final Set<Class<?>> testsToRun) {
        TestExecutionPlannerLoader testExecutionPlannerLoader = mock(TestExecutionPlannerLoader.class);
        when(testExecutionPlannerLoader.getVerifier())
            .thenReturn(
                className -> testsToRun.stream().map(Class::getName).anyMatch(name -> name.equals(className)));

        return testExecutionPlannerLoader;
    }
}
