package org.arquillian.smart.testing.strategies.affected.detector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import org.arquillian.smart.testing.strategies.affected.EndingWithTestTestVerifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class FileSystemTestClassDetectorTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_scan_for_tests_simple_projects() throws IOException {

        // given

        final File rootDirectory = temporaryFolder.newFolder("project");
        createProjectLayoutWithTests(rootDirectory, "org.mytest", "MyFirstTest.java",
            "MySecondTest.java", "Utils.java");

        final TestClassDetector testClassDetector =
            new FileSystemTestClassDetector(rootDirectory, new EndingWithTestTestVerifier());

        // when

        final Set<File> testClasses = testClassDetector.detect();

        // then

        assertThat(testClasses)
            .hasSize(2)
            .extracting(File::getName)
            .containsExactlyInAnyOrder("MyFirstTest.java", "MySecondTest.java");

    }

    @Test
    public void should_scan_for_tests_multimodule_projects() throws IOException {

        // given

        final File rootDirectory = temporaryFolder.newFolder("project");

        final Path moduleA = Files.createDirectories(Paths.get(rootDirectory.getAbsolutePath(), "moduleA"));
        createProjectLayoutWithTests(moduleA.toFile(), "org.mytest", "MyFirstTest.java",
            "MySecondTest.java", "Utils.java");

        final Path moduleB = Files.createDirectories(Paths.get(rootDirectory.getAbsolutePath(), "moduleB"));
        createProjectLayoutWithTests(moduleB.toFile(), "org.mytest", "MyThirdTest.java",
            "MyFourthTest.java", "Utils.java");

        final TestClassDetector testClassDetector =
            new FileSystemTestClassDetector(rootDirectory, new EndingWithTestTestVerifier());

        // when

        final Set<File> testClasses = testClassDetector.detect();

        // then

        assertThat(testClasses)
            .hasSize(4)
            .extracting(File::getName)
            .containsExactlyInAnyOrder("MyFirstTest.java", "MySecondTest.java", "MyThirdTest.java", "MyFourthTest.java");

    }

    private void createProjectLayoutWithTests(File directory, String packageDirectory, String... testNames) throws IOException {
        final String[] testDirectory = new String[] {"src", "test", "java"};
        final String[] packageDirectoryLocation = packageDirectory.split("\\.");

        final Path path = Paths.get(directory.getAbsolutePath(), concat(testDirectory, packageDirectoryLocation));

        Files.createDirectories(path);

        Arrays.stream(testNames)
            .map(Paths::get)
            .map(path::resolve)
            .forEach(testPath -> {
                try {
                    Files.createFile(testPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private String[] concat(String[] first, String[] second) {
        String[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
