package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.A;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.D;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyControllerObject;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.superbiz.Alone;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.superbiz.component.Unwanted;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.BTest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.CTest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.MySecondBusinessObjectTest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.YTest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.ZTest;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.arquillian.smart.testing.strategies.affected.AffectedTestsDetector.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class ClassDependenciesGraphTest {

    @Test
    public void should_detect_simple_test_to_execute() {
        // given
        final Configuration configuration = ConfigurationLoader.load();
        configuration.loadStrategyConfigurations(AFFECTED);

        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);

        final String testLocation = getClassLocation(MyBusinessObjectTest.class);
        classDependenciesGraph.buildTestDependencyGraph(singletonList(new File(testLocation)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(MyBusinessObject.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactly("org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest");
    }

    @Test
    public void should_detect_multiple_tests_to_execute_against_same_main_class() {
        // given
        final Configuration configuration = ConfigurationLoader.load();
        configuration.loadStrategyConfigurations(AFFECTED);

        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);

        buildTestDependencyGraphWithLocation(classDependenciesGraph);

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(MyBusinessObject.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MySecondBusinessObjectTest");
    }

    @Test
    public void should_detect_test_with_multiple_main_classes() {
        // given
        final Configuration configuration = ConfigurationLoader.load();
        configuration.loadStrategyConfigurations(AFFECTED);

        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);
        buildTestDependencyGraphWithLocation(classDependenciesGraph);

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(MyControllerObject.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactly(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MySecondBusinessObjectTest");
    }

    @Test
    public void should_detect_multiple_tests_to_execute_against_same_main_class_avoiding_duplicates() {
        // given
        final Configuration configuration = ConfigurationLoader.load();
        configuration.loadStrategyConfigurations(AFFECTED);

        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);
        buildTestDependencyGraphWithLocation(classDependenciesGraph);

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(MyBusinessObject.class)));
        mainObjectsChanged.add(new File(getClassLocation(MyControllerObject.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MySecondBusinessObjectTest");
    }


    @Test
    public void should_detect_all_changes_transitive() {
        // given
        final Configuration configuration = ConfigurationLoader.load();
        configuration.loadStrategyConfigurations(AFFECTED);

        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);
        buildTestDependencyGraphWithTestLocation(classDependenciesGraph);

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(D.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest", "org.arquillian.smart.testing.strategies.affected.fakeproject.test.BTest");
    }

    @Test
    public void should_detect_all_changes_adding_package_annotated_transitive() {
        // given
        final Configuration configuration = ConfigurationLoader.load();
        configuration.loadStrategyConfigurations(AFFECTED);

        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);

        final String testLocation = getClassLocation(ZTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(Unwanted.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ZTest");
    }

    @Test
    public void should_detect_all_changes_adding_class_package_annotated_transitive() {
        // given
        final Configuration configuration = ConfigurationLoader.load();
        configuration.loadStrategyConfigurations(AFFECTED);
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);

        final String testLocation = getClassLocation(YTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(Alone.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.YTest");
    }

    @Test
    public void should_not_detect_all_changes_transitive_if_transitivity_is_disabled() {
        // given
        final AffectedConfiguration affectedConfiguration = new AffectedConfiguration();
        affectedConfiguration.setTransitivity(false);

        final Configuration configuration = ConfigurationLoader.load();
        configuration.setStrategiesConfiguration(Collections.singletonList(affectedConfiguration));

        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);
        buildTestDependencyGraphWithTestLocation(classDependenciesGraph);

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(D.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .isEmpty();
    }

    @Test
    public void should_exclude_imports_if_property_set() {
        // given
        final AffectedConfiguration affectedConfiguration = new AffectedConfiguration();
        affectedConfiguration.setExclusions(
            Arrays.asList("org.arquillian.smart.testing.strategies.affected.fakeproject.main.B"));

        final Configuration configuration = ConfigurationLoader.load();
        configuration.setStrategiesConfiguration(Collections.singletonList(affectedConfiguration));

        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);
        buildTestDependencyGraphWithTestLocation(classDependenciesGraph);

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(D.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .isEmpty();
    }

    @Test
    public void should_include_only_imports_if_property_set() {
        // given
        final AffectedConfiguration affectedConfiguration = new AffectedConfiguration();
        affectedConfiguration.setInclusions(
            Collections.singletonList("org.arquillian.smart.testing.strategies.affected.fakeproject.main.A"));

        final Configuration configuration = ConfigurationLoader.load();
        configuration.setStrategiesConfiguration(Collections.singletonList(affectedConfiguration));

        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier(), configuration);
        buildTestDependencyGraphWithTestLocation(classDependenciesGraph);

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(A.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest");
    }

    private void buildTestDependencyGraphWithTestLocation(ClassDependenciesGraph classDependenciesGraph) {
        final String testLocation = getClassLocation(ATest.class);
        final String testLocation2 = getClassLocation(BTest.class);
        final String testLocation3 = getClassLocation(CTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));
    }

    private void buildTestDependencyGraphWithLocation(ClassDependenciesGraph classDependenciesGraph) {
        final String testLocation = getClassLocation(MyBusinessObjectTest.class);
        final String testLocation2 = getClassLocation(MySecondBusinessObjectTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));
    }

    private String getClassLocation(Class<?> clazz) {
        return clazz.getResource(clazz.getSimpleName() + ".class").getPath();
    }
}
