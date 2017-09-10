package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.D;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyControllerObject;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.BTest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.CTest;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;

import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ClassDependenciesGraphTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_detect_simple_test_to_execute() {
        // given
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactly("org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest");
    }

    @Test
    public void should_detect_multiple_tests_to_execute_against_same_main_class() {
        // given
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        final String testLocation2 = MyBusinessObjectTest.class.getResource("MySecondBusinessObjectTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));

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
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        final String testLocation2 = MyBusinessObjectTest.class.getResource("MySecondBusinessObjectTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(MyControllerObject.class.getResource("MyControllerObject.class").getPath()));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactly(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MySecondBusinessObjectTest");
    }

    @Test
    public void should_detect_multiple_tests_to_execute_against_same_main_class_avoiding_duplicates() {
        // given
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        final String testLocation2 = MyBusinessObjectTest.class.getResource("MySecondBusinessObjectTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));
        mainObjectsChanged.add(new File(MyControllerObject.class.getResource("MyControllerObject.class").getPath()));

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
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = ATest.class.getResource("ATest.class").getPath();
        final String testLocation2 = BTest.class.getResource("BTest.class").getPath();
        final String testLocation3 = CTest.class.getResource("CTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(D.class.getResource("D.class").getPath()));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest", "org.arquillian.smart.testing.strategies.affected.fakeproject.test.BTest");
    }

    @Test
    public void should_exclude_imports_if_property_set() {
        // given
        System.setProperty(AffectedRunnerProperties.SMART_TESTING_AFFECTED_EXCLUSIONS, "org.arquillian.smart.testing.strategies.affected.fakeproject.main.B");
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = ATest.class.getResource("ATest.class").getPath();
        final String testLocation2 = BTest.class.getResource("BTest.class").getPath();
        final String testLocation3 = CTest.class.getResource("CTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when
        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(D.class.getResource("D.class").getPath()));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then
        assertThat(testsDependingOn)
            .isEmpty();
    }

    @Test
    public void should_include_only_imports_if_property_set() {
        // given
        System.setProperty(AffectedRunnerProperties.SMART_TESTING_AFFECTED_INCLUSIONS, "org.arquillian.smart.testing.strategies.affected.fakeproject.main.A");
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = ATest.class.getResource("ATest.class").getPath();
        final String testLocation2 = BTest.class.getResource("BTest.class").getPath();
        final String testLocation3 = CTest.class.getResource("CTest.class").getPath();
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when

        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(D.class.getResource("A.class").getPath()));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then

        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest");
    }

}
