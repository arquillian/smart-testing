package org.arquillian.smart.testing.vcs.git;

import java.util.Collection;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.filter.TestVerifier;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.git.GitChangeResolver;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

import static org.arquillian.smart.testing.scm.ChangeType.ADD;

public class NewTestsDetector implements TestExecutionPlanner {

    private static final Logger logger = Logger.getLogger(NewTestsDetector.class);

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;
    private final TestVerifier testVerifier;

    // Temporary before introducing proper DI
    public NewTestsDetector(TestVerifier testVerifier) {
        this(new GitChangeResolver(), new JavaSPILoader().onlyOne(ChangeStorage.class).get(), testVerifier);
    }

    public NewTestsDetector(ChangeResolver changeResolver, ChangeStorage changeStorage, TestVerifier testVerifier) {
        this.changeResolver = changeResolver;
        this.changeStorage = changeStorage;
        this.testVerifier = testVerifier;
    }

    @Override
    public String getName() {
        return "new";
    }

    @Override
    public final Collection<TestSelection> getTests() {
        final Collection<Change> changes = changeStorage.read()
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff();
        });

        return changes.stream()
            .filter(change -> ADD.equals(change.getChangeType()))
            .filter(change -> testVerifier.isTest(change.getLocation()))
            .map(change -> new TestSelection(change.getLocation(), getName()))
            .collect(Collectors.toList());
    }

}
