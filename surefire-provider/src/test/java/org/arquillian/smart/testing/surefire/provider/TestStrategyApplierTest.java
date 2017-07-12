package org.arquillian.smart.testing.surefire.provider;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.jcip.annotations.NotThreadSafe;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@NotThreadSafe
public class TestStrategyApplierTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Mock
    private TestExecutionPlannerLoader testExecutionPlannerLoader;

    @Mock
    private ProviderParameters providerParameters;

    @Mock
    private TestExecutionPlanner testExecutionPlanner;

    @Test
    public void should_return_tests_only_from_strategies_when_filtering_mode_configured() {
        // given
        System.setProperty(Configuration.SMART_TESTING_MODE, "selecting");
        System.setProperty(Configuration.SMART_TESTING, "static");

        final Set<Class<?>> defaultTestsToRun = new HashSet<>();
        defaultTestsToRun.add(ProviderParameterParserTest.class);
        defaultTestsToRun.add(TestExecutionPlannerLoaderTest.class);

        final TestsToRun testsToRun = new TestsToRun(defaultTestsToRun);
        when(testExecutionPlannerLoader.getPlannerForStrategy("static")).thenReturn(testExecutionPlanner);
        when(providerParameters.getTestClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());

        final Set<String> strategyTests = new LinkedHashSet<>();
        strategyTests.add(TestExecutionPlannerLoaderTest.class.getName());

        when(testExecutionPlanner.getTests()).thenReturn(strategyTests);

        // when
        final Configuration configuration = Configuration.read();
        TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, testExecutionPlannerLoader, providerParameters.getTestClassLoader());
        final TestsToRun realTestPlanning = testStrategyApplier.apply(configuration);

        // then
        assertThat(realTestPlanning.getLocatedClasses())
            .hasSize(1)
            .containsExactly(TestExecutionPlannerLoaderTest.class);

    }

    @Test
    public void should_return_tests_ordered_by_default() {

        // given
        System.setProperty(Configuration.SMART_TESTING, "static");

        final Set<Class<?>> defaultTestsToRun = new LinkedHashSet<>();
        defaultTestsToRun.add(ProviderParameterParserTest.class);
        defaultTestsToRun.add(TestStrategyApplierTest.class);
        defaultTestsToRun.add(TestExecutionPlannerLoaderTest.class);

        final TestsToRun testsToRun = new TestsToRun(defaultTestsToRun);

        when(testExecutionPlannerLoader.getPlannerForStrategy("static")).thenReturn(testExecutionPlanner);

        final Set<String> strategyTests = new LinkedHashSet<>();
        strategyTests.add(TestExecutionPlannerLoaderTest.class.getName());

        when(testExecutionPlanner.getTests()).thenReturn(strategyTests);

        // when
        final Configuration configuration = Configuration.read();
        TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, testExecutionPlannerLoader, Thread.currentThread().getContextClassLoader());
        final TestsToRun realTestPlanning = testStrategyApplier.apply(configuration);

        // then
        assertThat(realTestPlanning.getLocatedClasses())
            .hasSize(3)
            .containsExactly(TestExecutionPlannerLoaderTest.class, ProviderParameterParserTest.class, TestStrategyApplierTest.class);

    }

    @Test
    public void should_not_return_test_from_strategies_if_it_is_not_in_class_path() {
        // given
        System.setProperty(Configuration.SMART_TESTING_MODE, "selecting");
        System.setProperty(Configuration.SMART_TESTING, "static");

        final Set<Class<?>> defaultTestsToRun = new HashSet<>();
        defaultTestsToRun.add(ProviderParameterParserTest.class);
        defaultTestsToRun.add(TestExecutionPlannerLoaderTest.class);

        final TestsToRun testsToRun = new TestsToRun(defaultTestsToRun);
        when(testExecutionPlannerLoader.getPlannerForStrategy("static")).thenReturn(testExecutionPlanner);

        final Set<String> strategyTests = new LinkedHashSet<>();
        strategyTests.add(TestExecutionPlannerLoaderTest.class.getName());
        strategyTests.add("org.arquillian.smart.testing.vcs.git.ChangedFilesDetectorTest");

        when(testExecutionPlanner.getTests()).thenReturn(strategyTests);

        // when
        final Configuration configuration = Configuration.read();
        TestStrategyApplier testStrategyApplier = new TestStrategyApplier(testsToRun, testExecutionPlannerLoader, Thread.currentThread().getContextClassLoader());
        final TestsToRun realTestPlanning = testStrategyApplier.apply(configuration);

        // then

        assertThat(realTestPlanning.getLocatedClasses())
            .hasSize(1)
            .containsExactly(TestExecutionPlannerLoaderTest.class);

    }
}
