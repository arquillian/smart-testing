package org.arquillian.smart.testing.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestExecutionPlannerLoaderTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final File projectDir = new File(System.getProperty("user.dir"));

    @Test
    public void should_find_matching_strategy() throws Exception {
        // given
        final JavaSPILoader mockedSpiLoader = mock(JavaSPILoader.class);
        when(mockedSpiLoader.all(eq(TestExecutionPlannerFactory.class))).thenAnswer(i -> Collections.singletonList(
            new DummyTestExecutionPlannerFactory()));
        final TestExecutionPlannerLoaderImpl testExecutionPlannerLoader =
            new TestExecutionPlannerLoaderImpl(mockedSpiLoader, resource -> true, projectDir);

        // when
        final TestExecutionPlanner testExecutionPlanner = testExecutionPlannerLoader.getPlannerForStrategy("dummy");

        assertThat(testExecutionPlanner.getTests()).isEmpty();
    }

    @Test
    public void should_throw_exception_when_no_matching_strategy_found() throws Exception {
        // given
        final JavaSPILoader mockedSpiLoader = mock(JavaSPILoader.class);
        when(mockedSpiLoader.all(eq(TestExecutionPlannerFactory.class))).thenAnswer(i -> Collections.singletonList(
            new DummyTestExecutionPlannerFactory()));
        final TestExecutionPlannerLoaderImpl testExecutionPlannerLoader =
            new TestExecutionPlannerLoaderImpl(mockedSpiLoader, resource -> true, projectDir);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("No strategy found for [new]. Available strategies are: [[dummy]]. Please make sure you have corresponding dependency defined.");

        // when
        final TestExecutionPlanner exceptionShouldBeThrown = testExecutionPlannerLoader.getPlannerForStrategy("new");
    }

    @Test
    public void should_throw_exception_when_no_strategies_found() throws Exception {
        // given
        final JavaSPILoader mockedSpiLoader = mock(JavaSPILoader.class);
        when(mockedSpiLoader.all(eq(TestExecutionPlannerFactory.class))).thenReturn(Collections.emptyList());
        final TestExecutionPlannerLoaderImpl testExecutionPlannerLoader =
            new TestExecutionPlannerLoaderImpl(mockedSpiLoader, resource -> true, projectDir);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("There is no strategy available. Please make sure you have corresponding dependencies defined.");

        // when
        final TestExecutionPlanner exceptionShouldBeThrown = testExecutionPlannerLoader.getPlannerForStrategy("new");
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
        public TestExecutionPlanner create(File projectDir, TestVerifier verifier) {
            return new TestExecutionPlanner() {
                @Override
                public Collection<TestSelection> getTests() {
                    return Collections.emptyList();
                }

                @Override
                public String getName() {
                    return "static";
                }
            };
        }
    }
}
