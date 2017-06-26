package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.arquillian.smart.testing.strategies.affected.detector.FileSystemTestClassDetector;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AffectedChangesDetectorTest {

    @Mock
    FileSystemTestClassDetector fileSystemTestClassDetector;

    @Before
    public void configureMocks() {
        Set<File> testClasses = new HashSet<>();
        testClasses.add(new File(
            "src/test/java/org/arquillian/smart/testing/strategies/affected/fakeproject/test/MyBusinessObjectTest.java"));
        testClasses.add(new File(
            "src/test/java/org/arquillian/smart/testing/strategies/affected/fakeproject/test/MyBusinessObjectTestCase.java"));
        when(fileSystemTestClassDetector.detect()).thenReturn(testClasses);
    }

    @Test
    public void should_get_affected_tests_by_a_main_class_change() {

        // given
        when(fileSystemTestClassDetector.getGlobPatterns()).thenReturn(Collections.singletonList("**/*Test.java"));

        final Set<File> mainClasses = new HashSet<>();
        mainClasses.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));

        final AffectedChangesDetector affectedChangesDetector =
            new AffectedChangesDetector(fileSystemTestClassDetector, mainClasses);

        // when
        final Collection<String> tests = affectedChangesDetector.getTests();

        // then
        assertThat(tests)
            .hasSize(1)
            .containsExactly("org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest");
    }

    @Test
    public void should_not_get_affected_tests_by_a_main_class_change_if_no_pattern() {
        //given
        when(fileSystemTestClassDetector.getGlobPatterns()).thenReturn(Collections.emptyList());

        final Set<File> mainClasses = new HashSet<>();
        mainClasses.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));

        final AffectedChangesDetector affectedChangesDetector =
            new AffectedChangesDetector(fileSystemTestClassDetector, mainClasses);

        // when
        final Collection<String> tests = affectedChangesDetector.getTests();

        // then
        assertThat(tests)
            .hasSize(0);
    }

    @Test
    public void should_get_affected_tests_by_a_main_class_change_filtered_as_per_pattern() {

        // given
        when(fileSystemTestClassDetector.getGlobPatterns()).thenReturn(Collections.singletonList("**/*TestCase.java"));

        final Set<File> mainClasses = new HashSet<>();
        mainClasses.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));
        final AffectedChangesDetector affectedChangesDetector =
            new AffectedChangesDetector(fileSystemTestClassDetector, mainClasses);

        // when
        final Collection<String> tests = affectedChangesDetector.getTests();

        // then
        assertThat(tests)
            .hasSize(1)
            .containsExactly(
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTestCase");
    }

    @Test
    public void should_get_all_affected_tests_by_a_main_class_change_filtered_as_per_pattern() {

        // given
        when(fileSystemTestClassDetector.getGlobPatterns()).thenReturn(
            Arrays.asList("**/*TestCase.java", "**/*Test.java"));

        final Set<File> mainClasses = new HashSet<>();
        mainClasses.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));
        final AffectedChangesDetector affectedChangesDetector =
            new AffectedChangesDetector(fileSystemTestClassDetector, mainClasses);

        // when
        final Collection<String> tests = affectedChangesDetector.getTests();

        // then
        assertThat(tests)
            .hasSize(2)
            .containsExactly("org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTestCase",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest");
    }
}
