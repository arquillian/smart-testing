package org.arquillian.smart.testing.surefire.provider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestStrategyApplierTest {

    @Mock
    private ProviderParametersParser providerParametersParser;

    @Mock
    private TestExecutionPlannerLoader testExecutionPlannerLoader;

    @Mock
    private TestExecutionPlanner testExecutionPlanner;

    @Test
    public void should_return_tests_from_strategies_by_default() {

        // given

        final Set<Class<?>> defaultTestsToRun = new HashSet<>();
        defaultTestsToRun.add(ProviderParameterParserTest.class);

        final TestsToRun testsToRun = new TestsToRun(defaultTestsToRun);

        when(providerParametersParser.getProperty("order")).thenReturn(null);
        when(testExecutionPlannerLoader.getPlannerForStrategy("static")).thenReturn(testExecutionPlanner);

        final Set<String> strategyTests = new LinkedHashSet<>();
        strategyTests.add(TestExecutionPlannerLoaderTest.class.getName());

        when(testExecutionPlanner.getTests()).thenReturn(strategyTests);

        // when

        TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, providerParametersParser, testExecutionPlannerLoader);
        final TestsToRun realTestPlanning = testStrategyApplier.apply(Arrays.asList("static"));

        // then

        assertThat(realTestPlanning.getLocatedClasses())
            .hasSize(1)
            .containsExactly(TestExecutionPlannerLoaderTest.class);

    }

    @Test
    public void should_return_tests_ordered_first_strategies_then_rest_when_configured_with_order_option() {

        // given

        final Set<Class<?>> defaultTestsToRun = new LinkedHashSet<>();
        defaultTestsToRun.add(ProviderParameterParserTest.class);
        defaultTestsToRun.add(TestStrategyApplierTest.class);

        final TestsToRun testsToRun = new TestsToRun(defaultTestsToRun);

        when(providerParametersParser.getProperty("order")).thenReturn("true");
        when(testExecutionPlannerLoader.getPlannerForStrategy("static")).thenReturn(testExecutionPlanner);

        final Set<String> strategyTests = new LinkedHashSet<>();
        strategyTests.add(TestExecutionPlannerLoaderTest.class.getName());

        when(testExecutionPlanner.getTests()).thenReturn(strategyTests);

        // when

        TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, providerParametersParser, testExecutionPlannerLoader);
        final TestsToRun realTestPlanning = testStrategyApplier.apply(Arrays.asList("static"));

        // then

        assertThat(realTestPlanning.getLocatedClasses())
            .hasSize(3)
            .containsExactly(TestExecutionPlannerLoaderTest.class, ProviderParameterParserTest.class, TestStrategyApplierTest.class);

    }

}
