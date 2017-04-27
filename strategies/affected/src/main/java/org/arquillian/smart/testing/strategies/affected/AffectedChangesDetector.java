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

    private Set<File> changedClasses = new HashSet<>();

    public AffectedChangesDetector(final File projectDirectory, final Set<File> changedClasses) {
        this.testClassDetector = new FileSystemTestClassDetector(projectDirectory);
        this.changedClasses.addAll(changedClasses);
    }

    @Override
    public Collection<String> getTests() {

        // TODO we need to figure out what really works in terms of classpath locations such as when running on Maven or containing classes inside a JAR file
        final ClassFileIndex classFileIndex = new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), ""));

        final Set<File> allTestsOfCurrentProject = this.testClassDetector.detect();
        classFileIndex.addTestClasses(allTestsOfCurrentProject);

        return classFileIndex.findTestsDependingOn(this.changedClasses);
    }

    /**
     * Setter for testing purposes
     * @param testClassDetector
     */
    void setTestClassDetector(TestClassDetector testClassDetector) {
        this.testClassDetector = testClassDetector;
    }
}
