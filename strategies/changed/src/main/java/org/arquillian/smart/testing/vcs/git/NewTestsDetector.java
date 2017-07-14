package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ClassNameExtractor;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.ChangeType;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

import static org.arquillian.smart.testing.filter.GlobPatternMatcher.matchPatterns;

public class NewTestsDetector implements TestExecutionPlanner {

    private final ChangeResolver changeResolver;
    private final ChangeStorage changeStorage;
    private final String[] globPatterns;

    public NewTestsDetector(File currentDir, String previous, String head, String... globPatterns) {
        // TODO SPI it
        this.changeResolver =
            new org.arquillian.smart.testing.scm.git.GitChangeResolver(currentDir, previous, head);
        this.changeStorage = new JavaSPILoader().onlyOne(ChangeStorage.class).get();
        this.globPatterns = globPatterns;
    }

    @Override
    public Collection<String> getTests() {
        final Collection<Change> files = changeStorage.read()
            .orElseGet(() -> {
                // TODO better logging
                System.out.println("We didn't find cached changes... rolling back to direct resolution");
                return changeResolver.diff();
        });

        return files.stream()
            .filter(change -> ChangeType.ADD.equals(change.getChangeType()))
            // to have an interface called TestDecider.isTest(path) -> PatternTestDecider. include/globs etc
            .filter(change -> matchPatterns(change.getLocation().toAbsolutePath().toString(),
                this.globPatterns))
            .map(change -> new ClassNameExtractor().extractFullyQualifiedName(change.getLocation().toFile()))
            .collect(Collectors.toList());
    }

}
