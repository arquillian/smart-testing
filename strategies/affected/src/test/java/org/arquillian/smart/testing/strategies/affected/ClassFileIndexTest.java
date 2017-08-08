package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.A;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.B;
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

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class ClassFileIndexTest {


    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_detect_simple_test_to_execute() {

        // given

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""),
            Collections.singletonList("**/*Test.java"));

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        classFileIndex.buildTestDependencyGraph(Arrays.asList(new File(testLocation)));

        // when

        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));

        final Set<String> testsDependingOn = classFileIndex.findTestsDependingOn(mainObjectsChanged);

        // then

        assertThat(testsDependingOn)
            .containsExactly("org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest");
    }

    @Test
    public void should_detect_multiple_tests_to_execute_against_same_main_class() {

        // given

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""),
            Collections.singletonList("**/*Test.java"));

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        final String testLocation2 = MyBusinessObjectTest.class.getResource("MySecondBusinessObjectTest.class").getPath();
        classFileIndex.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));

        // when

        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));

        final Set<String> testsDependingOn = classFileIndex.findTestsDependingOn(mainObjectsChanged);

        // then

        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MySecondBusinessObjectTest");
    }

    @Test
    public void should_detect_test_with_multiple_main_classes() {

        // given

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""),
            Collections.singletonList("**/*Test.java"));

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        final String testLocation2 = MyBusinessObjectTest.class.getResource("MySecondBusinessObjectTest.class").getPath();
        classFileIndex.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));

        // when

        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(MyControllerObject.class.getResource("MyControllerObject.class").getPath()));

        final Set<String> testsDependingOn = classFileIndex.findTestsDependingOn(mainObjectsChanged);

        // then

        assertThat(testsDependingOn)
            .containsExactly(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MySecondBusinessObjectTest");
    }

    @Test
    public void should_detect_multiple_tests_to_execute_against_same_main_class_avoiding_duplicates() {

        // given

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""),
            Collections.singletonList("**/*Test.java"));

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        final String testLocation2 = MyBusinessObjectTest.class.getResource("MySecondBusinessObjectTest.class").getPath();
        classFileIndex.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2)));

        // when

        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));
        mainObjectsChanged.add(new File(MyControllerObject.class.getResource("MyControllerObject.class").getPath()));

        final Set<String> testsDependingOn = classFileIndex.findTestsDependingOn(mainObjectsChanged);

        // then

        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MySecondBusinessObjectTest");
    }

    @Test
    public void should_only_detect_one_level_import_depth_by_default() {
        // given

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""),
            Collections.singletonList("**/*Test.java"));

        final String testLocation = ATest.class.getResource("ATest.class").getPath();
        final String testLocation2 = BTest.class.getResource("BTest.class").getPath();
        final String testLocation3 = CTest.class.getResource("CTest.class").getPath();
        classFileIndex.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when

        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(A.class.getResource("A.class").getPath()));

        final Set<String> testsDependingOn = classFileIndex.findTestsDependingOn(mainObjectsChanged);

        // then

        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest");
    }

    @Test
    public void sholud_detect_changes_on_configured_depth_import_level() {
        // given

        System.setProperty(AffectedRunnerProperties.SMART_TESTING_DEPTH_LEVEL, "2");

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""),
            Collections.singletonList("**/*Test.java"));

        final String testLocation = ATest.class.getResource("ATest.class").getPath();
        final String testLocation2 = BTest.class.getResource("BTest.class").getPath();
        final String testLocation3 = CTest.class.getResource("CTest.class").getPath();
        classFileIndex.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when

        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(B.class.getResource("B.class").getPath()));

        final Set<String> testsDependingOn = classFileIndex.findTestsDependingOn(mainObjectsChanged);

        // then

        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest", "org.arquillian.smart.testing.strategies.affected.fakeproject.test.BTest");
    }

    @Test
    public void should_detect_all_changes_when_configured_depth_is_greater_than_current_depth() {
        // given

        System.setProperty(AffectedRunnerProperties.SMART_TESTING_DEPTH_LEVEL, Integer.toString(Integer.MAX_VALUE));

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""),
            Collections.singletonList("**/*Test.java"));

        final String testLocation = ATest.class.getResource("ATest.class").getPath();
        final String testLocation2 = BTest.class.getResource("BTest.class").getPath();
        final String testLocation3 = CTest.class.getResource("CTest.class").getPath();
        classFileIndex.buildTestDependencyGraph(Arrays.asList(new File(testLocation), new File(testLocation2),
            new File(testLocation3)));

        // when

        Set<File> mainObjectsChanged = new HashSet<>();
        mainObjectsChanged.add(new File(D.class.getResource("D.class").getPath()));

        final Set<String> testsDependingOn = classFileIndex.findTestsDependingOn(mainObjectsChanged);

        // then

        assertThat(testsDependingOn)
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.ATest", "org.arquillian.smart.testing.strategies.affected.fakeproject.test.BTest");
    }

}
