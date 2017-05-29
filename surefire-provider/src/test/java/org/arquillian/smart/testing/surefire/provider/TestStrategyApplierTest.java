package org.arquillian.smart.testing.surefire.provider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
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
    private ProviderParameters providerParameters;

    @Mock
    private TestExecutionPlanner testExecutionPlanner;

    @Test
    public void should_return_tests_only_from_strategies_when_filtering_mode_configured() {

        // given

        final Set<Class<?>> defaultTestsToRun = new HashSet<>();
        defaultTestsToRun.add(ProviderParameterParserTest.class);
        defaultTestsToRun.add(TestExecutionPlannerLoaderTest.class);

        final TestsToRun testsToRun = new TestsToRun(defaultTestsToRun);

        when(providerParametersParser.containsProperty(TestStrategyApplier.USAGE)).thenReturn(true);
        when(providerParametersParser.getProperty(TestStrategyApplier.USAGE)).thenReturn(RunMode.SELECTING.name().toLowerCase());
        when(testExecutionPlannerLoader.getPlannerForStrategy("static")).thenReturn(testExecutionPlanner);
        when(providerParameters.getTestClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());

        final Set<String> strategyTests = new LinkedHashSet<>();
        strategyTests.add(TestExecutionPlannerLoaderTest.class.getName());

        when(testExecutionPlanner.getTests()).thenReturn(strategyTests);

        // when

        TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, providerParametersParser, testExecutionPlannerLoader, providerParameters);
        final TestsToRun realTestPlanning = testStrategyApplier.apply(Arrays.asList("static"));

        // then

        assertThat(realTestPlanning.getLocatedClasses())
            .hasSize(1)
            .containsExactly(TestExecutionPlannerLoaderTest.class);

    }

    @Test
    public void should_return_tests_ordered_by_default() {

        // given

        final Set<Class<?>> defaultTestsToRun = new LinkedHashSet<>();
        defaultTestsToRun.add(ProviderParameterParserTest.class);
        defaultTestsToRun.add(TestStrategyApplierTest.class);
        defaultTestsToRun.add(TestExecutionPlannerLoaderTest.class);

        final TestsToRun testsToRun = new TestsToRun(defaultTestsToRun);

        when(providerParametersParser.containsProperty(TestStrategyApplier.USAGE)).thenReturn(false);
        when(testExecutionPlannerLoader.getPlannerForStrategy("static")).thenReturn(testExecutionPlanner);
        when(providerParameters.getTestClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());

        final Set<String> strategyTests = new LinkedHashSet<>();
        strategyTests.add(TestExecutionPlannerLoaderTest.class.getName());

        when(testExecutionPlanner.getTests()).thenReturn(strategyTests);

        // when

        TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, providerParametersParser, testExecutionPlannerLoader, providerParameters);
        final TestsToRun realTestPlanning = testStrategyApplier.apply(Arrays.asList("static"));

        // then

        assertThat(realTestPlanning.getLocatedClasses())
            .hasSize(3)
            .containsExactly(TestExecutionPlannerLoaderTest.class, ProviderParameterParserTest.class, TestStrategyApplierTest.class);

    }

    @Test
    public void should_not_return_test_from_strategies_if_it_is_not_in_class_path() {
        // given

        final Set<Class<?>> defaultTestsToRun = new HashSet<>();
        defaultTestsToRun.add(ProviderParameterParserTest.class);
        defaultTestsToRun.add(TestExecutionPlannerLoaderTest.class);

        final TestsToRun testsToRun = new TestsToRun(defaultTestsToRun);

        when(providerParametersParser.containsProperty(TestStrategyApplier.USAGE)).thenReturn(true);
        when(providerParametersParser.getProperty(TestStrategyApplier.USAGE)).thenReturn(RunMode.SELECTING.name().toLowerCase());
        when(testExecutionPlannerLoader.getPlannerForStrategy("static")).thenReturn(testExecutionPlanner);
        when(providerParameters.getTestClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());

        final Set<String> strategyTests = new LinkedHashSet<>();
        strategyTests.add(TestExecutionPlannerLoaderTest.class.getName());
        strategyTests.add("org.arquillian.smart.testing.vcs.git.ChangedFilesDetectorTest");

        when(testExecutionPlanner.getTests()).thenReturn(strategyTests);

        // when

        TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, providerParametersParser, testExecutionPlannerLoader, providerParameters);
        final TestsToRun realTestPlanning = testStrategyApplier.apply(Arrays.asList("static"));

        // then

        assertThat(realTestPlanning.getLocatedClasses())
            .hasSize(1)
            .containsExactly(TestExecutionPlannerLoaderTest.class);

    }
}
