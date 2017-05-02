package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyControllerObject;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassFileIndexTest {

    @Test
    public void should_detect_simple_test_to_execute() {

        // given

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""));

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        classFileIndex.addTestJavaFiles(Arrays.asList(new File(testLocation)));

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

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""));

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        final String testLocation2 = MyBusinessObjectTest.class.getResource("MySecondBusinessObjectTest.class").getPath();
        classFileIndex.addTestJavaFiles(Arrays.asList(new File(testLocation), new File(testLocation2)));

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

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""));

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        final String testLocation2 = MyBusinessObjectTest.class.getResource("MySecondBusinessObjectTest.class").getPath();
        classFileIndex.addTestJavaFiles(Arrays.asList(new File(testLocation), new File(testLocation2)));

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

        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""));

        final String testLocation = MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath();
        final String testLocation2 = MyBusinessObjectTest.class.getResource("MySecondBusinessObjectTest.class").getPath();
        classFileIndex.addTestJavaFiles(Arrays.asList(new File(testLocation), new File(testLocation2)));

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
}
