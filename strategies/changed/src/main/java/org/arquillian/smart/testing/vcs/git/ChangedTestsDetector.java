package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

import static org.arquillian.smart.testing.scm.ChangeType.MODIFY;
import static org.arquillian.smart.testing.scm.ChangeType.RENAME;

public class ChangedTestsDetector implements TestExecutionPlanner {

    static final String CHANGED = "changed";

    private static final Logger logger = Log.getLogger();

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;
    private final File projectDir;
    private final TestVerifier testVerifier;
    private final Configuration configuration;

    public ChangedTestsDetector(File projectDir, TestVerifier testVerifier, Configuration configuration) {
        this(new JavaSPILoader().onlyOne(ChangeResolver.class).get(),
            new JavaSPILoader().onlyOne(ChangeStorage.class).get(),
            projectDir,
            testVerifier,
            configuration);
    }

    public ChangedTestsDetector(ChangeResolver changeResolver, ChangeStorage changeStorage, File projectDir,
        TestVerifier testVerifier, Configuration configuration) {
        this.changeResolver = changeResolver;
        this.changeStorage = changeStorage;
        this.projectDir = projectDir;
        this.testVerifier = testVerifier;
        this.configuration = configuration;
    }

    @Override
    public String getName() {
        return CHANGED;
    }

    @Override
    public Collection<TestSelection> selectTestsFromNames(Iterable<String> testsToRun) {
        return getTests();
    }

    @Override
    public Collection<TestSelection> selectTestsFromClasses(Iterable<Class<?>> testsToRun) {
        return getTests();
    }

    public Collection<TestSelection> getTests() {
        //tag::read_changes[]
        final Collection<Change> files = changeStorage.read(projectDir) // <1>
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff(projectDir, configuration, getName()); // <2>
            });
        //end::read_changes[]
        return files.stream()
            .filter(change -> EnumSet.of(MODIFY, RENAME).contains(change.getChangeType()))
            .filter(change -> testVerifier.isTest(change.getLocation()))
            .map(change -> new TestSelection(change.getLocation(), getName()))
            .collect(Collectors.toList());
    }


}
