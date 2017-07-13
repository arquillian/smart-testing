package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.strategies.affected.detector.FileSystemTestClassDetector;
import org.arquillian.smart.testing.strategies.affected.detector.TestClassDetector;

import static org.arquillian.smart.testing.filter.GlobPatternMatcher.matchPatterns;

public class AffectedTestsDetector implements TestExecutionPlanner {

    // TODO TestClassDetector is something that can be moved to extension
    private final TestClassDetector testClassDetector;
    private final String classpath;

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;
    private final String mainClassesLocationPattern;

    public AffectedTestsDetector(File currentDir, String previous, String head, final TestClassDetector testClassDetector) {
        this(currentDir, previous, head, testClassDetector, "");
    }

    public AffectedTestsDetector(File currentDir, String previous, String head, final TestClassDetector testClassDetector,
        String classpath) {
        this.testClassDetector = testClassDetector;
        this.changeResolver =
            new org.arquillian.smart.testing.scm.git.GitChangeResolver(currentDir, previous, head);
        this.changeStorage = new JavaSPILoader().onlyOne(ChangeStorage.class).get();
        this.classpath = classpath;
        this.mainClassesLocationPattern = "**/src/main/java/**/*.java";
    }

    AffectedTestsDetector(TestClassDetector testClassDetector, ChangeStorage changeStorage, ChangeResolver changeResolver, String mainClassesLocationPattern) {
        this.testClassDetector = testClassDetector;
        this.changeStorage = changeStorage;
        this.changeResolver = changeResolver;
        this.classpath = "";
        this.mainClassesLocationPattern = mainClassesLocationPattern;
    }

    @Override
    public Collection<String> getTests() {
        ClassFileIndex classFileIndex = configureTestClassDetector();

        // Scan disk finding all tests of current project
        // TODO this operations hould be done in extension to avoid scanning for all modules. In case of Arquillian core is an improvement of 500 ms per module
        final Set<File> allTestsOfCurrentProject = this.testClassDetector.detect();
        classFileIndex.addTestJavaFiles(allTestsOfCurrentProject);

        final Collection<Change> files = changeStorage.read()
            .orElseGet(() -> {
                // TODO better logging
                System.out.println("We didn't find cached changes... rolling back to direct resolution");
                return changeResolver.diff();
            });

        final Set<File> mainClasses = files.stream()
            .filter(change -> ChangeType.ADD.equals(change.getChangeType()))
            // to have an interface called TestDecider.isCoreClass(path) -> PatternTestDecider. include/globs etc
            .filter(change -> matchPatterns(change.getLocation().toAbsolutePath().toString(),
                mainClassesLocationPattern))
            .map(change -> change.getLocation().toFile())
            .collect(Collectors.toSet());

        return classFileIndex.findTestsDependingOn(mainClasses);
    }

    private ClassFileIndex configureTestClassDetector() {
        ClassFileIndex classFileIndex;
        if (testClassDetector instanceof FileSystemTestClassDetector) {
            FileSystemTestClassDetector fileSystemTestClassDetector = (FileSystemTestClassDetector) testClassDetector;
            classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), this.classpath),
                fileSystemTestClassDetector.getGlobPatterns());
        } else {
            classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), this.classpath));
        }
        return classFileIndex;
    }
}
