package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
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
public class AffectedTestsDetectorTest {

    @Mock
    FileSystemTestClassDetector fileSystemTestClassDetector;

    @Mock
    ChangeResolver changeResolver;

    @Mock
    ChangeStorage changeStorage;

    @Before
    public void configureMocks() {
        Set<File> testClasses = new HashSet<>();
        testClasses.add(new File(
            "src/test/java/org/arquillian/smart/testing/strategies/affected/fakeproject/test/MyBusinessObjectTest.java").getAbsoluteFile());
        testClasses.add(new File(
            "src/test/java/org/arquillian/smart/testing/strategies/affected/fakeproject/test/MyBusinessObjectTestCase.java").getAbsoluteFile());
        when(fileSystemTestClassDetector.detect()).thenReturn(testClasses);
    }

    @Test
    public void should_get_affected_tests_by_a_main_class_change() {

        // given
        when(fileSystemTestClassDetector.getGlobPatterns()).thenReturn(Collections.singletonList("**/src/test/java/**/*.java"));


        Change change = new Change(getJavaPath(MyBusinessObject.class), ChangeType.ADD);
        when(changeStorage.read()).thenReturn(Optional.of(Arrays.asList(change)));

        final AffectedTestsDetector affectedTestsDetector =
            new AffectedTestsDetector(fileSystemTestClassDetector, changeStorage, changeResolver, "**/src/test/java/**/*.java");

        // when
        final Collection<TestSelection> tests = affectedTestsDetector.getTests();

        // then
        assertThat(tests)
            .extracting(TestSelection::getClassName)
            .hasSize(2)
            .contains("org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTest", "org.arquillian.smart.testing.strategies.affected.fakeproject.test.MyBusinessObjectTestCase");
    }

    private Path getJavaPath(Class<?> clazz) {
        final String packageDirectory = clazz.getPackage().getName().replace(".", "/");
        final Path path = Paths.get("src/test/java", packageDirectory, clazz.getSimpleName() + ".java");

        return path.toAbsolutePath();
    }
}
