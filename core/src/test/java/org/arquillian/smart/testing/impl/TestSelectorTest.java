package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSelectorTest {

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void should_return_merged_test_selections_if_test_selection_has_same_class() {
        // given
        final TestSelection testSelectionNew = new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "new");
        final TestSelection testSelectionChanged =
            new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "changed");

        final Set<Class<?>> classes =
            new LinkedHashSet<>(Collections.singletonList(TestExecutionPlannerLoaderTest.class));
        TestExecutionPlannerLoader testExecutionPlannerLoader = prepareLoader(classes);

        final DummyTestSelector testSelector =
            new DummyTestSelector(ConfigurationLoader.load(tmpFolder.getRoot()), testExecutionPlannerLoader,
                new File("."));

        // when
        final Collection<TestSelection> testSelections =
            testSelector.filterMergeAndOrderTestSelection(asList(testSelectionNew, testSelectionChanged),
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
            new TestSelection(TestSelectorTest.class.getName(), "changed");

        final Set<Class<?>> classes =
            new LinkedHashSet<>(asList(TestExecutionPlannerLoaderTest.class, TestSelectorTest.class));
        TestExecutionPlannerLoader testExecutionPlannerLoader = prepareLoader(classes);

        final DummyTestSelector testSelector =
            new DummyTestSelector(ConfigurationLoader.load(tmpFolder.getRoot()), testExecutionPlannerLoader,
                new File("."));

        // when
        final Collection<TestSelection> testSelections =
            testSelector.filterMergeAndOrderTestSelection(asList(testSelectionNew, testSelectionChanged),
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

    class DummyTestSelector extends TestSelector {

        DummyTestSelector(Configuration configuration,
            TestExecutionPlannerLoader testExecutionPlannerLoader, File projectDir) {
            super(configuration, testExecutionPlannerLoader, projectDir);
        }

        @Override
        protected Collection<TestSelection> selectTestUsingPlanner(TestExecutionPlanner plannerForStrategy) {
            return Collections.emptyList();
        }

        @Override
        protected Iterable getTestsToRun() {
            return Collections.emptyList();
        }

        @Override
        protected TestSelection createTestSelection(Object testClass) {
            return null;
        }
    }
}
