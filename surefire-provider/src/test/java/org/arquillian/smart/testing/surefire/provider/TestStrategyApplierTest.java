package org.arquillian.smart.testing.surefire.provider;

import java.util.Collection;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.TestSelection;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;

@RunWith(MockitoJUnitRunner.class)
public class TestStrategyApplierTest {

    @Mock
    private TestsToRun testsToRun;

    @Mock
    private TestExecutionPlannerLoader testExecutionPlannerLoader;

    @Test
    public void should_return_merged_test_selections_if_test_selection_has_same_class() {
        // given
        final TestSelection
            testSelectionNew = new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "new");
        final TestSelection testSelectionChanged = new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "changed");
        final TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, testExecutionPlannerLoader, Thread.currentThread().getContextClassLoader());

        // when
        final Collection<TestSelection> testSelections =
            testStrategyApplier.filterAndMergeTestSelection(asList(testSelectionNew, testSelectionChanged));

        //then
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
        final TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, testExecutionPlannerLoader, Thread.currentThread().getContextClassLoader());

        // when
        final Collection<TestSelection> testSelections =
            testStrategyApplier.filterAndMergeTestSelection(asList(testSelectionNew, testSelectionChanged));


        // then
        Assertions.assertThat(testSelections)
            .hasSize(2)
            .flatExtracting("types").containsExactly("new", "changed");
    }
}
