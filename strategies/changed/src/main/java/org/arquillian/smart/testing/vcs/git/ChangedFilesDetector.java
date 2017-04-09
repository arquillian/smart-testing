package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.eclipse.jgit.diff.DiffEntry;

public class ChangedFilesDetector extends GitChangesDetector {

    public ChangedFilesDetector(File repoRoot, String previous, String head, String... globPatterns) {
        super(repoRoot, previous, head, globPatterns);
    }

    @Override
    protected boolean isMatching(DiffEntry diffEntry) {
        return DiffEntry.ChangeType.MODIFY == diffEntry.getChangeType()
            && matchPatterns(diffEntry.getNewPath());
    }
}
