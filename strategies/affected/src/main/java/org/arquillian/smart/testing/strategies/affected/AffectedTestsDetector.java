package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.strategies.affected.detector.TestClassDetector;

public class AffectedTestsDetector implements TestExecutionPlanner {

    private static final
    Logger logger = Log.getLogger();

    // TODO TestClassDetector is something that can be moved to extension
    private final TestClassDetector testClassDetector;

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;
    private final TestVerifier testVerifier;

    AffectedTestsDetector(final TestClassDetector testClassDetector, TestVerifier testVerifier) {
        this(testClassDetector, new JavaSPILoader().onlyOne(ChangeStorage.class).get(),
            new JavaSPILoader().onlyOne(ChangeResolver.class).get(), testVerifier);
    }

    AffectedTestsDetector(TestClassDetector testClassDetector, ChangeStorage changeStorage, ChangeResolver changeResolver, TestVerifier testVerifier) {
        this.testClassDetector = testClassDetector;
        this.changeStorage = changeStorage;
        this.changeResolver = changeResolver;
        this.testVerifier = testVerifier;
    }

    @Override
    public String getName() {
        return "affected";
    }

    @Override
    public Collection<TestSelection> getTests() {
        ClassDependenciesGraph classDependenciesGraph = configureTestClassDetector();

        // TODO this operations should be done in extension to avoid scanning for all modules.
        // TODO In case of Arquillian core is an improvement of 500 ms per module
        // Scan disk finding all tests of current project

        final long beforeDetection = System.currentTimeMillis();

        final Set<File> allTestsOfCurrentProject = this.testClassDetector.detect();
        classDependenciesGraph.buildTestDependencyGraph(allTestsOfCurrentProject);

        final Collection<Change> files = changeStorage.read()
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff();
            });

        logger.debug("Time To Build Affected Dependencies Graph %d ms", (System.currentTimeMillis() - beforeDetection));

        final Set<File> mainClasses = files.stream()
            .map(Change::getLocation)
            .filter(testVerifier::isCore)
            .map(Path::toFile)
            .collect(Collectors.toSet());

        final long beforeFind = System.currentTimeMillis();

        final Set<TestSelection> affected = classDependenciesGraph.findTestsDependingOn(mainClasses)
            .stream()
            .map(s -> new TestSelection(s, "affected"))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        logger.debug("Time To Find Affected Tests %d ms", (System.currentTimeMillis() - beforeFind));

        return affected;
    }

    private ClassDependenciesGraph configureTestClassDetector() {
        return new ClassDependenciesGraph(testVerifier);
    }
}
