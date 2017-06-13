package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.Logger;
import org.eclipse.jgit.diff.DiffEntry;

public class NewFilesDetector extends GitChangesDetector {

    private static final Logger logger = Logger.getLogger(NewFilesDetector.class.getName());

    public NewFilesDetector(File currentDir, String previous, String head, String ... globPatterns) {
        super(currentDir, previous, head, globPatterns);
    }

    @Override
    public Collection<String> getTests() {
        final Collection<String> tests = super.getTests();

        final Set<String> files = this.gitChangeResolver.uncommitted();
        final List<String> notCommittedTests = files.stream()
            .filter(this::matchPatterns)
            .map(file -> {
                try {
                    final File sourceFile = new File(repoRoot, file);
                    return extractFullyQualifiedName(sourceFile);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .peek(test -> logger.log(Level.FINEST, String.format("%s test added because not committed", test)))
            .collect(Collectors.toList());

        tests.addAll(notCommittedTests);

        return tests;
    }

    @Override
    public Set<File> getFiles() {

        final Set<File> files = super.getFiles();
        final Set<String> uncommittedFiles = gitChangeResolver.uncommitted();

        files.addAll(
                uncommittedFiles.stream()
                .filter(this::matchPatterns)
                .map(file -> new File(repoRoot, file))
                .collect(Collectors.toSet())
        );

        return files;
    }

    protected boolean isMatching(DiffEntry diffEntry) {
        return DiffEntry.ChangeType.ADD == diffEntry.getChangeType()
            && matchPatterns(diffEntry.getNewPath());
    }

}
