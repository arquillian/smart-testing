package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.arquillian.smart.testing.scm.git.GitChangeResolver;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

public class ChangedTestsDetector implements TestExecutionPlanner {

    private static final Logger logger = Logger.getLogger();

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;
    private final TestVerifier testVerifier;

    public ChangedTestsDetector(File projectDir, TestVerifier testVerifier) {
        this(new GitChangeResolver(projectDir), new JavaSPILoader().onlyOne(ChangeStorage.class).get(), testVerifier);
    }

    public ChangedTestsDetector(ChangeResolver changeResolver, ChangeStorage changeStorage, TestVerifier testVerifier) {
        this.changeResolver = changeResolver;
        this.changeStorage = changeStorage;
        this.testVerifier = testVerifier;
    }

    @Override
    public String getName() {
        return "changed";
    }

    @Override
    public Collection<TestSelection> getTests() {
        final Collection<Change> files = changeStorage.read()
            .orElseGet(() -> {
                logger.warn("No cached changes detected... using direct resolution");
                return changeResolver.diff();
            });

        return files.stream()
            .filter(change -> ChangeType.MODIFY.equals(change.getChangeType()))
            .filter(change -> testVerifier.isTest(change.getLocation()))
            .map(change -> new TestSelection(change.getLocation(), getName()))
            .collect(Collectors.toList());
    }


}
