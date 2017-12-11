package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.StrategyConfiguration;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestExecutionPlannerLoaderTest {

    private final File projectDir = new File(System.getProperty("user.dir"));

    @Test
    public void should_find_matching_strategy() throws Exception {
        // given
        final JavaSPILoader mockedSpiLoader = mock(JavaSPILoader.class);
        when(mockedSpiLoader.all(eq(TestExecutionPlannerFactory.class))).thenAnswer(i -> Collections.singletonList(
            new DummyTestExecutionPlannerFactory()));
        final TestExecutionPlannerLoaderImpl testExecutionPlannerLoader =
            new TestExecutionPlannerLoaderImpl(mockedSpiLoader, resource -> true, projectDir, mock(Configuration.class));

        // when
        final TestExecutionPlanner testExecutionPlanner = testExecutionPlannerLoader.getPlannerForStrategy("dummy");

        // then
        assertThat(testExecutionPlanner.selectTestsFromClasses(Collections.emptyList())).isEmpty();
    }

    @Test
    public void should_autocorrent_and_find_matching_strategy() throws Exception {
        // given
        final JavaSPILoader mockedSpiLoader = mock(JavaSPILoader.class);
        when(mockedSpiLoader.all(eq(TestExecutionPlannerFactory.class))).thenAnswer(i -> Collections.singletonList(
            new DummyTestExecutionPlannerFactory()));
        Configuration configuration = new Configuration();
        configuration.setAutocorrect(true);
        configuration.setStrategies("dumy");
        final TestExecutionPlannerLoaderImpl testExecutionPlannerLoader =
            new TestExecutionPlannerLoaderImpl(mockedSpiLoader, resource -> true, projectDir, configuration);

        // when
        configuration.autocorrectStrategies(testExecutionPlannerLoader.getAvailableStrategyNames(), new ArrayList<>());
        final TestExecutionPlanner testExecutionPlanner = testExecutionPlannerLoader.getPlannerForStrategy(configuration.getStrategies()[0]);

        // then
        assertThat(testExecutionPlanner.selectTestsFromClasses(Collections.emptyList())).isEmpty();
    }

    @Test
    public void should_throw_exception_when_no_matching_strategy_found() throws Exception {
        // given
        final JavaSPILoader mockedSpiLoader = mock(JavaSPILoader.class);
        when(mockedSpiLoader.all(eq(TestExecutionPlannerFactory.class))).thenAnswer(i -> Collections.singletonList(
            new DummyTestExecutionPlannerFactory()));
        final TestExecutionPlannerLoaderImpl testExecutionPlannerLoader =
            new TestExecutionPlannerLoaderImpl(mockedSpiLoader, resource -> true, projectDir, mock(Configuration.class));

        // when
        final Throwable exception = catchThrowable(() -> testExecutionPlannerLoader.getPlannerForStrategy("new"));

        // then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No strategy found for [new]. Available strategies are: [[dummy]]. Please make sure you have corresponding dependency defined.");

    }

    @Test
    public void should_throw_exception_when_no_strategies_found() throws Exception {
        // given
        final JavaSPILoader mockedSpiLoader = mock(JavaSPILoader.class);
        when(mockedSpiLoader.all(eq(TestExecutionPlannerFactory.class))).thenReturn(Collections.emptyList());
        final TestExecutionPlannerLoaderImpl testExecutionPlannerLoader =
            new TestExecutionPlannerLoaderImpl(mockedSpiLoader, resource -> true, projectDir, mock(Configuration.class));

        // when
        final Throwable exception = catchThrowable(() -> testExecutionPlannerLoader.getPlannerForStrategy("new"));

        // then
        assertThat(exception).isInstanceOf(IllegalStateException.class)
            .hasMessage("There is no strategy available. Please make sure you have corresponding dependencies defined.");
    }

    private static class DummyTestExecutionPlannerFactory implements TestExecutionPlannerFactory {
        @Override
        public String alias() {
            return "dummy";
        }

        @Override
        public boolean isFor(String name) {
            return alias().equalsIgnoreCase(name);
        }

        @Override
        public TestExecutionPlanner create(File projectDir, TestVerifier verifier, Configuration configuration) {
            return new TestExecutionPlanner() {

                @Override
                public Collection<TestSelection> selectTestsFromNames(Iterable<String> testsToRun) {
                    return Collections.emptyList();
                }

                @Override
                public Collection<TestSelection> selectTestsFromClasses(Iterable<Class<?>> testsToRun) {
                    return Collections.emptyList();
                }

                @Override
                public String getName() {
                    return "static";
                }
            };
        }

        @Override
        public StrategyConfiguration strategyConfiguration() {
            return null;
        }
    }
}
