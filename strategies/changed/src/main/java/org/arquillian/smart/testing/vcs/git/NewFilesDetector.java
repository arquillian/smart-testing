package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import org.eclipse.jgit.diff.DiffEntry;

public class NewFilesDetector extends GitChangesDetector {

    public NewFilesDetector(File repoRoot, String previous, String head, String ... globPatterns) {
        super(repoRoot, previous, head, globPatterns);
    }

    protected boolean isMatching(DiffEntry diffEntry) {
        return DiffEntry.ChangeType.ADD == diffEntry.getChangeType()
            && matchPatterns(diffEntry.getNewPath());
    }

}
