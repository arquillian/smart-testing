package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.strategies.affected.detector.FileSystemTestClassDetector;
import org.arquillian.smart.testing.strategies.affected.detector.TestClassDetector;

public class AffectedChangesDetector implements TestExecutionPlanner {

    private TestClassDetector testClassDetector;
    private Set<File> changedJavaFiles = new HashSet<>();
    private String classpath;

    public AffectedChangesDetector(final TestClassDetector testClassDetector, final Set<File> changedJavaFiles) {
        this(testClassDetector, changedJavaFiles, "");
    }

    public AffectedChangesDetector(final TestClassDetector testClassDetector, final Set<File> changedJavaFiles,
        String classpath) {
        this.testClassDetector = testClassDetector;
        this.changedJavaFiles.addAll(changedJavaFiles);
        this.classpath = classpath;
    }

    @Override
    public Collection<String> getTests() {
        ClassFileIndex classFileIndex;
        // TODO we need to figure out what really works in terms of classpath locations such as when running on Maven or containing classes inside a JAR file
        if (testClassDetector instanceof FileSystemTestClassDetector) {
            FileSystemTestClassDetector fileSystemTestClassDetector = (FileSystemTestClassDetector) testClassDetector;
            classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), this.classpath),
                fileSystemTestClassDetector.getGlobPatterns());
        } else {
            classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), this.classpath));
        }

        final Set<File> allTestsOfCurrentProject = this.testClassDetector.detect();
        classFileIndex.addTestJavaFiles(allTestsOfCurrentProject);

        return classFileIndex.findTestsDependingOn(this.changedJavaFiles);
    }
}
