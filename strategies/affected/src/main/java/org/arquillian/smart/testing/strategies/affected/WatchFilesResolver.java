package org.arquillian.smart.testing.strategies.affected;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.arquillian.smart.testing.strategies.affected.ast.JavaClass;

class WatchFilesResolver {

    private Path projectDir;

    WatchFilesResolver(Path projectDir) {
        this.projectDir  = projectDir;
    }

    List<Path> resolve(JavaClass testJavaClass) {
        final List<Path> files = new ArrayList<>();
        final WatchFile[] allTestsAnnotation = findWatchFiles(testJavaClass);

        for (WatchFile file : allTestsAnnotation) {
            files.add(projectDir.resolve(file.value()).normalize());
        }

        return files;
    }

    private WatchFile[] findWatchFiles(JavaClass testJavaClass) {

        final Optional<WatchFiles> testsListOptional = testJavaClass.getAnnotationByType(WatchFiles.class);

        WatchFile[] tests = testsListOptional
            .map(WatchFiles::value)
            .orElseGet(() -> testJavaClass.getAnnotationByType(WatchFile.class)
                .map(annotation -> new WatchFile[] {annotation})
                .orElse(new WatchFile[0]));

        return tests;
    }

}
