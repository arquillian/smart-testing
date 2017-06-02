package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.jgit.diff.DiffEntry;

public class ChangedFilesDetector extends GitChangesDetector {

    private static final Logger logger = Logger.getLogger(ChangedFilesDetector.class.getName());

    public ChangedFilesDetector(File currentDir, String previous, String head, String... globPatterns) {
        super(currentDir, previous, head, globPatterns);
    }

    @Override
    public Collection<String> getTests() {
        final Collection<String> tests = super.getTests();

        final Set<String> files = this.gitChangeResolver.modifiedChanges();
        final List<String> modifiedLocalTests = files.stream()
            .filter(this::matchPatterns)
            .map(file -> {
                try {
                    final File sourceFile = new File(repoRoot, file);
                    return extractFullyQualifiedName(sourceFile);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .peek(test -> logger.log(Level.FINEST, String.format("%s test added either because modified or staged as modified file", test)))
            .collect(Collectors.toList());

        tests.addAll(modifiedLocalTests);

        return tests;
    }

    @Override
    public Set<File> getFiles() {

        final Set<File> files = super.getFiles();
        final Set<String> modifiedLocalFiles = gitChangeResolver.modifiedChanges();

        files.addAll(
            modifiedLocalFiles.stream()
                .filter(this::matchPatterns)
                .map(file -> new File(repoRoot, file))
                .collect(Collectors.toSet())
        );

        return files;
    }

    @Override
    protected boolean isMatching(DiffEntry diffEntry) {
        return DiffEntry.ChangeType.MODIFY == diffEntry.getChangeType()
            && matchPatterns(diffEntry.getNewPath());
    }
}
