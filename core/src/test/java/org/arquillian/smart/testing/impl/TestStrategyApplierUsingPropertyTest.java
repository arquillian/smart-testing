package org.arquillian.smart.testing.impl;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.ClassNameExtractorTest;
import org.arquillian.smart.testing.FilesCodecTest;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.TestSelectionTest;
import org.arquillian.smart.testing.api.SmartTesting;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import static org.arquillian.smart.testing.Constants.CURRENT_DIR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Category(NotThreadSafe.class)
public class TestStrategyApplierUsingPropertyTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_return_tests_only_relevant_for_defined_strategies_when_selecting_mode_configured() {
        // given
        System.setProperty(Configuration.SMART_TESTING_MODE, "selecting");
        System.setProperty(Configuration.SMART_TESTING, "static");

        final Set<Class<?>> testsToRun = createTestsToRun(ClassNameExtractorTest.class, TestSelectionTest.class);

        final Set<TestSelection> strategyTests = new LinkedHashSet<>();
        strategyTests.add(new TestSelection(TestSelectionTest.class.getName(), "static"));

        TestExecutionPlannerLoader testExecutionPlannerLoader = prepareLoader(testsToRun, strategyTests);

        // when
        Set<TestSelection> optimizedClasses =
            new ConfiguredSmartTestingImpl(testExecutionPlannerLoader, ConfigurationLoader.load(CURRENT_DIR)).applyOnClasses(
                testsToRun);

        // then
        Assertions.assertThat(SmartTesting.getClasses(optimizedClasses))
            .hasSize(1)
            .containsExactly(TestSelectionTest.class);
    }

    @Test
    public void should_return_tests_selected_by_default() {
        // given
        System.setProperty(Configuration.SMART_TESTING, "static");

        final Set<Class<?>> testsToRun =
            createTestsToRun(ClassNameExtractorTest.class, TestStrategyApplierUsingPropertyTest.class,
                TestSelectionTest.class);

        final Set<TestSelection> strategyTests = new LinkedHashSet<>();
        strategyTests.add(new TestSelection(TestSelectionTest.class.getName(), "static"));

        TestExecutionPlannerLoader testExecutionPlannerLoader = prepareLoader(testsToRun, strategyTests);

        // when
        Set<TestSelection> optimizedClasses =
            new ConfiguredSmartTestingImpl(testExecutionPlannerLoader, ConfigurationLoader.load(CURRENT_DIR)).applyOnClasses(
                testsToRun);

        // then
        Assertions.assertThat(SmartTesting.getClasses(optimizedClasses))
            .containsExactly(TestSelectionTest.class);
    }

    @Test
    public void should_return_all_tests_when_ordering_selected() {
        // given
        System.setProperty(Configuration.SMART_TESTING_MODE, "ordering");
        System.setProperty(Configuration.SMART_TESTING, "static");

        final Set<Class<?>> testsToRun =
            createTestsToRun(ClassNameExtractorTest.class, TestStrategyApplierUsingPropertyTest.class,
                TestSelectionTest.class);

        final Set<TestSelection> strategyTests = new LinkedHashSet<>();
        strategyTests.add(new TestSelection(TestSelectionTest.class.getName(), "static"));

        TestExecutionPlannerLoader testExecutionPlannerLoader = prepareLoader(testsToRun, strategyTests);

        // when
        Set<TestSelection> optimizedClasses =
            new ConfiguredSmartTestingImpl(testExecutionPlannerLoader, ConfigurationLoader.load(CURRENT_DIR)).applyOnClasses(
                testsToRun);

        // then
        Assertions.assertThat(SmartTesting.getClasses(optimizedClasses))
            .containsExactly(TestSelectionTest.class, ClassNameExtractorTest.class,
                TestStrategyApplierUsingPropertyTest.class);
    }

    @Test
    public void should_not_return_test_from_strategies_if_it_is_not_in_class_path_or_in_tests_to_run() {
        // given
        System.setProperty(Configuration.SMART_TESTING_MODE, "selecting");
        System.setProperty(Configuration.SMART_TESTING, "static");

        final Set<Class<?>> testsToRun = createTestsToRun(ClassNameExtractorTest.class, TestSelectionTest.class);

        final Set<TestSelection> strategyTests = new LinkedHashSet<>();
        strategyTests.add(new TestSelection(TestSelectionTest.class.getName(), "static"));
        strategyTests.add(new TestSelection(FilesCodecTest.class.getName(), "static"));
        strategyTests.add(new TestSelection("org.arquillian.smart.testing.vcs.git.ChangedFilesDetectorTest", "static"));

        TestExecutionPlannerLoader testExecutionPlannerLoader = prepareLoader(testsToRun, strategyTests);

        // when
        Set<TestSelection> optimizedClasses =
            new ConfiguredSmartTestingImpl(testExecutionPlannerLoader, ConfigurationLoader.load(CURRENT_DIR)).applyOnClasses(
                testsToRun);

        // then
        Assertions.assertThat(SmartTesting.getClasses(optimizedClasses))
            .hasSize(1)
            .containsExactly(TestSelectionTest.class);
    }

    private TestExecutionPlannerLoader prepareLoader(final Set<Class<?>> testsToRun, Set<TestSelection> strategyTests) {
        TestExecutionPlanner testExecutionPlanner = mock(TestExecutionPlanner.class);
        when(testExecutionPlanner.selectTestsFromClasses(Mockito.anyCollection())).thenReturn(strategyTests);

        TestExecutionPlannerLoader testExecutionPlannerLoader = mock(TestExecutionPlannerLoader.class);
        when(testExecutionPlannerLoader.getPlannerForStrategy("static")).thenReturn(testExecutionPlanner);
        when(testExecutionPlannerLoader.getVerifier())
            .thenReturn(className -> testsToRun.stream().map(Class::getName).anyMatch(name -> name.equals(className)));

        return testExecutionPlannerLoader;
    }

    private Set<Class<?>> createTestsToRun(Class... testClasses) {
        return new LinkedHashSet<>(Arrays.<Class<?>>asList(testClasses));
    }
}
