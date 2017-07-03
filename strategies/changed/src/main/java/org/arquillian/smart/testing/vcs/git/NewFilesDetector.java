package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.diff.DiffEntry;

public class NewFilesDetector extends GitChangesDetector {

    public NewFilesDetector(File currentDir, String previous, String head, String... globPatterns) {
        super(currentDir, previous, head, globPatterns);
    }

    @Override
    public Collection<String> getTests() {
        final Collection<String> tests = super.getTests();

        final Set<String> files = this.gitChangeResolver.newChanges();
        List<String> newLocalTests = getLocalTests(files);
        tests.addAll(newLocalTests);

        return tests;
    }

    @Override
    public Set<File> getFiles() {

        final Set<File> files = super.getFiles();
        final Set<String> newLocalFiles = gitChangeResolver.newChanges();

        appendLocalFiles(files, newLocalFiles);

        return files;
    }

    protected boolean isMatching(DiffEntry diffEntry) {
        return DiffEntry.ChangeType.ADD == diffEntry.getChangeType()
            && matchPatterns(diffEntry.getNewPath());
    }
}
