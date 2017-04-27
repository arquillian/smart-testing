package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.arquillian.smart.testing.strategies.affected.detector.TestClassDetector;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject;
import org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest;
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
    TestClassDetector testClassDetector;

    @Before
    public void configureMocks() {
        Set<File> testClasses = new HashSet<>();
        testClasses.add(new File(MyBusinessObjectTest.class.getResource("MyBusinessObjectTest.class").getPath()));
        when(testClassDetector.detect()).thenReturn(testClasses);
    }

    @Test
    public void should_calculate_which_tests_are_affected_by_a_main_class_change() {

        // given

        final Set<File> mainClasses = new HashSet<>();
        mainClasses.add(new File(MyBusinessObject.class.getResource("MyBusinessObject.class").getPath()));

        final AffectedChangesDetector affectedChangesDetector = new AffectedChangesDetector(new File("."), mainClasses);
        affectedChangesDetector.setTestClassDetector(testClassDetector);

        // when

        final Collection<String> tests = affectedChangesDetector.getTests();

        // then

        assertThat(tests)
            .hasSize(1)
            .containsExactly("org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest");
    }

}
