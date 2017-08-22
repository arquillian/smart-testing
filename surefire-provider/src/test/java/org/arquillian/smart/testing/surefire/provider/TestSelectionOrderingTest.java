package org.arquillian.smart.testing.surefire.provider;

import java.util.Collection;
import java.util.List;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.TestSelection;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Arrays.asList;

public class TestSelectionOrderingTest {

    @Mock
    private TestsToRun testsToRun;

    @Mock
    private TestExecutionPlannerLoader testExecutionPlannerLoader;

    @Test
    public void should_order_test_selection_with_previous_strategy_configuration() {
        // given
        final TestSelection testSelectionNew = new TestSelection(TestSelectionOrderingTest.class.getName(), "new");
        final TestSelection testSelectionNewOther = new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "new");
        final TestSelection testSelectionChanged = new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "changed");
        final TestSelection testSelectionChangedAnother = new TestSelection(TestStrategyApplierTest.class.getName(), "changed");
        final TestSelection testSelectionAffected = new TestSelection(TestStrategyApplierTest.class.getName(), "affected");
        final TestSelection testSelectionAffectedAnother = new TestSelection(SmartTestingProviderTest.class.getName(), "affected");

        final List<TestSelection> selections = asList(testSelectionNew, testSelectionNewOther, testSelectionChangedAnother,
            testSelectionChanged, testSelectionAffected, testSelectionAffectedAnother);

        final TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, testExecutionPlannerLoader, Thread.currentThread().getContextClassLoader());

        final TestSelection newChangedMerged = new TestSelection(TestExecutionPlannerLoaderTest.class.getName(), "new", "changed");
        final TestSelection ChangedAffectedMerged = new TestSelection(TestStrategyApplierTest.class.getName(),"changed", "affected");

        // when
        final Collection<TestSelection> testSelections = testStrategyApplier.filterMergeAndOrderTestSelection(selections, asList("new", "changed", "affected"));

        //then
        Assertions.assertThat(testSelections)
            .hasSize(4)
            .containsExactly(newChangedMerged, testSelectionNew, ChangedAffectedMerged, testSelectionAffectedAnother)
            .flatExtracting("types").containsExactly("new", "changed", "new", "changed", "affected", "affected");
    }
}
