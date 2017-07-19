package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.diff.DiffEntry;

public class ChangedFilesDetector extends GitChangesDetector {

    public ChangedFilesDetector(File currentDir, String previous, String head, String... globPatterns) {
        super(currentDir, previous, head, globPatterns);
    }

    @Override
    public Collection<String> getTests() {
        final Collection<String> tests = super.getTests();

        final Set<String> files = this.gitChangeResolver.modifiedChanges();
        List<String> modifiedLocalTests = getLocalTests(files);
        tests.addAll(modifiedLocalTests);

        return tests;
    }

    @Override
    public Set<File> getFiles() {
        final Set<File> files = super.getFiles();
        final Set<String> modifiedLocalFiles = gitChangeResolver.modifiedChanges();

        Set<File> filteredModifiedLocalFiles = filterLocalFiles(modifiedLocalFiles);
        files.addAll(filteredModifiedLocalFiles);

        return files;
    }

    @Override
    protected boolean isMatching(DiffEntry diffEntry) {
        return DiffEntry.ChangeType.MODIFY == diffEntry.getChangeType()
            && matchPatterns(new File(super.repoRoot, diffEntry.getNewPath()).getAbsolutePath());
    }
}
