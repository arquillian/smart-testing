package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.filter.TestVerifier;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.strategies.affected.detector.TestClassDetector;

import static org.arquillian.smart.testing.filter.GlobPatternMatcher.matchPatterns;

public class AffectedTestsDetector implements TestExecutionPlanner {

    private static final Logger logger = Logger.getLogger(AffectedTestsDetector.class);

    // TODO TestClassDetector is something that can be moved to extension
    private final TestClassDetector testClassDetector;
    private final String classpath;

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;
    private final TestVerifier testVerifier;

    public AffectedTestsDetector(final TestClassDetector testClassDetector,
        String classpath, TestVerifier testVerifier) {
        this(testClassDetector, new JavaSPILoader().onlyOne(ChangeStorage.class).get(),
            new JavaSPILoader().onlyOne(ChangeResolver.class).get(), classpath,
            testVerifier);
    }

    AffectedTestsDetector(TestClassDetector testClassDetector, ChangeStorage changeStorage, ChangeResolver changeResolver,
        String classpath, TestVerifier testVerifier) {
        this.testClassDetector = testClassDetector;
        this.changeStorage = changeStorage;
        this.changeResolver = changeResolver;
        this.classpath = classpath;
        this.testVerifier = testVerifier;
    }

    @Override
    public String getName() {
        return "affected";
    }

    @Override
    public Collection<TestSelection> getTests() {
        ClassFileIndex classFileIndex = configureTestClassDetector();

        // TODO this operations should be done in extension to avoid scanning for all modules.
        // TODO In case of Arquillian core is an improvement of 500 ms per module
        // Scan disk finding all tests of current project

        final long beforeDetection = System.currentTimeMillis();

        final Set<File> allTestsOfCurrentProject = this.testClassDetector.detect();
        classFileIndex.buildTestDependencyGraph(allTestsOfCurrentProject);

        final Collection<Change> files = changeStorage.read()
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff();
            });

        logger.log(Level.FINER, "Time To Build Affected Dependencies Graph %d ms",
            (System.currentTimeMillis() - beforeDetection));

        final Set<File> mainClasses = files.stream()
            .map(Change::getLocation)
            .filter(testVerifier::isCore)
            .map(Path::toFile)
            .collect(Collectors.toSet());

        final long beforeFind = System.currentTimeMillis();

        final LinkedHashSet<TestSelection> affected = classFileIndex.findTestsDependingOn(mainClasses)
            .stream()
            .map(s -> new TestSelection(s, "affected"))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        logger.log(Level.FINER, "Time To Find Affected Tests %d ms", (System.currentTimeMillis() - beforeFind));

        return affected;
    }

    private ClassFileIndex configureTestClassDetector() {
        ClassFileIndex classFileIndex =
            new ClassFileIndex(new StandaloneClasspath(Collections.emptyList(), this.classpath), testVerifier);
        return classFileIndex;
    }
}
