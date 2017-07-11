package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.diff.DiffEntry;

/**
 * The intention of this class is not to provide a final strategy to final user but to be used by other strategies that
 * requires all changes of Git commits.
 */
public class AllChangedFilesDetector extends GitChangesDetector {

    public AllChangedFilesDetector(File currentDir, String previous, String head, String... globPatterns) {
        super(currentDir, previous, head, globPatterns);
    }

    public Collection<String> getTests() {
        final Collection<String> tests = super.getTests();

        final Set<String> newFiles = gitChangeResolver.newChanges();
        final Set<String> modifiedFiles = gitChangeResolver.modifiedChanges();

        List<String> newLocalTests = getLocalTests(newFiles);
        List<String> modifiedLocalTests = getLocalTests(modifiedFiles);

        tests.addAll(newLocalTests);
        tests.addAll(modifiedLocalTests);

        return tests;
    }

    public Set<File> getFiles() {
        final Set<File> files = super.getFiles();
        final Set<String> newLocalFiles = gitChangeResolver.newChanges();
        final Set<String> modifiedLocalFiles = gitChangeResolver.modifiedChanges();

        Set<File> filteredNewLocalFiles = filterLocalFiles(newLocalFiles);
        Set<File> filteredModifiedLocalFiles = filterLocalFiles(modifiedLocalFiles);

        files.addAll(filteredNewLocalFiles);
        files.addAll(filteredModifiedLocalFiles);

        return files;
    }

    @Override
    protected boolean isMatching(DiffEntry diffEntry) {
        return (DiffEntry.ChangeType.MODIFY == diffEntry.getChangeType()
            || DiffEntry.ChangeType.ADD == diffEntry.getChangeType())
            && matchPatterns(new File(super.repoRoot, diffEntry.getNewPath()).getAbsolutePath());
    }
}

