package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.A;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;

import static java.util.Collections.singletonList;
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
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = getClassLocation(MyBusinessObjectTest.class);
        final String testLocation2 = getClassLocation(MySecondBusinessObjectTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));

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
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = getClassLocation(MyBusinessObjectTest.class);
        final String testLocation2 = getClassLocation(MySecondBusinessObjectTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));

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
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = getClassLocation(MyBusinessObjectTest.class);
        final String testLocation2 = getClassLocation(MySecondBusinessObjectTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));

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
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = getClassLocation(ATest.class);
        final String testLocation2 = getClassLocation(BTest.class);
        final String testLocation3 = getClassLocation(CTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

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
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

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
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

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
        System.setProperty(AffectedRunnerProperties.SMART_TESTING_AFFECTED_TRANSITIVITY, "false");
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = getClassLocation(ATest.class);
        final String testLocation2 = getClassLocation(BTest.class);
        final String testLocation3 = getClassLocation(CTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

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
        System.setProperty(AffectedRunnerProperties.SMART_TESTING_AFFECTED_EXCLUSIONS, "org.arquillian.smart.testing.strategies.affected.fakeproject.main.B");
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = getClassLocation(ATest.class);
        final String testLocation2 = getClassLocation(BTest.class);
        final String testLocation3 = getClassLocation(CTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

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
        System.setProperty(AffectedRunnerProperties.SMART_TESTING_AFFECTED_INCLUSIONS, "org.arquillian.smart.testing.strategies.affected.fakeproject.main.A");
        final ClassDependenciesGraph
            classDependenciesGraph = new ClassDependenciesGraph(new EndingWithTestTestVerifier());

        final String testLocation = getClassLocation(ATest.class);
        final String testLocation2 = getClassLocation(BTest.class);
        final String testLocation3 = getClassLocation(CTest.class);
        classDependenciesGraph.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when

        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(getClassLocation(A.class)));

        final Set<String> testsDependingOn = classDependenciesGraph.findTestsDependingOn(mainObjectsChanged);

        // then

        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest");
    }

    private String getClassLocation(Class<?> clazz) {
        return clazz.getResource(clazz.getSimpleName() + ".class").getPath();
    }

}
