package org.arquillian.smart.testing.vcs.git;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * The intention of this class is not to provide a final strategy to final user but to be used by other strategies that requires all changes of Git commits.
 */
public class AllChanges {

    private NewFilesDetector newFilesDetector;
    private ChangedFilesDetector changedFilesDetector;

    public AllChanges(File currentDir, String previous, String head, String ... globPatterns) {
        newFilesDetector = new NewFilesDetector(currentDir, previous, head, globPatterns);
        changedFilesDetector = new ChangedFilesDetector(currentDir, previous, head, globPatterns);
    }

    public Set<String> getTests() {
        final Set<String> tests = new HashSet<>();

        tests.addAll(newFilesDetector.getTests());
        tests.addAll(changedFilesDetector.getTests());

        return tests;
    }

    public Set<File> getFiles() {
        final Set<File> files = new HashSet<>();

        files.addAll(newFilesDetector.getFiles());
        files.addAll(changedFilesDetector.getFiles());

        return files;

    }

}
