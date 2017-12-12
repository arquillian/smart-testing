package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.arquillian.smart.testing.strategies.affected.ast.JavaAssistClassParser;
import org.arquillian.smart.testing.strategies.affected.ast.JavaClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class WatchFilesResolverTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_read_watch_file_annotation() {

        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(SingleAnnotationTest.class.getName());
        final File root = temporaryFolder.getRoot();
        final WatchFilesResolver watchFilesResolver = new WatchFilesResolver(root.toPath());

        // when
        final List<Path> watchedFiles = watchFilesResolver.resolve(testClass);

        // then
        assertThat(watchedFiles)
            .containsExactlyInAnyOrder(Paths.get(root.getAbsolutePath(), "src/main/resources/persistence.xml"));
    }

    @Test
    public void should_read_multiple_watch_file_annotation() {

        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(MultipleAnnotationTest.class.getName());
        final File root = temporaryFolder.getRoot();
        final WatchFilesResolver watchFilesResolver = new WatchFilesResolver(root.toPath());

        // when
        final List<Path> watchedFiles = watchFilesResolver.resolve(testClass);

        // then
        assertThat(watchedFiles)
            .containsExactlyInAnyOrder(
                Paths.get(root.getAbsolutePath(), "src/main/resources/persistence.xml"),
                Paths.get(root.getAbsolutePath(), "src/main/resources/persistence2.xml"));
    }

    @Test
    public void should_read_multiple_watch_files_annotation() {

        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(MultipleAnnotationWatchFilesTest.class.getName());
        final File root = temporaryFolder.getRoot();
        final WatchFilesResolver watchFilesResolver = new WatchFilesResolver(root.toPath());

        // when
        final List<Path> watchedFiles = watchFilesResolver.resolve(testClass);

        // then
        assertThat(watchedFiles)
            .containsExactlyInAnyOrder(
                Paths.get(root.getAbsolutePath(), "src/main/resources/persistence.xml"),
                Paths.get(root.getAbsolutePath(), "src/main/resources/persistence2.xml"));
    }

    @Test
    public void should_read_watch_file_from_extended_annotation() {

        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(ExtendedWatchFileTest.class.getName());
        final File root = temporaryFolder.getRoot();
        final WatchFilesResolver watchFilesResolver = new WatchFilesResolver(root.toPath());

        // when
        final List<Path> watchedFiles = watchFilesResolver.resolve(testClass);

        // then
        assertThat(watchedFiles)
            .containsExactlyInAnyOrder(Paths.get(root.getAbsolutePath(), "src/main/resources/persistence.xml"));
    }

    @Ignore("Test ignored because it is used internally")
    public static class ExtendedWatchFileTest extends AbstractSingleAnnotation {

    }

    @WatchFile("src/main/resources/persistence.xml")
    public static class AbstractSingleAnnotation {
    }

    @Ignore("Test ignored because it is used internally")
    @WatchFiles({
        @WatchFile("src/main/resources/persistence.xml"),
        @WatchFile("src/main/resources/persistence2.xml")}
    )
    public static class MultipleAnnotationWatchFilesTest {
    }

    @Ignore("Test ignored because it is used internally")
    @WatchFile("src/main/resources/persistence.xml")
    @WatchFile("src/main/resources/persistence2.xml")
    public static class MultipleAnnotationTest {
    }

    @Ignore("Test ignored because it is used internally")
    @WatchFile("src/main/resources/persistence.xml")
    public static class SingleAnnotationTest {
    }

}
