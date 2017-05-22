package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Set;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;
import org.arquillian.smart.testing.strategies.affected.detector.FileSystemTestClassDetector;
import org.arquillian.smart.testing.vcs.git.AllChangedFilesDetector;

public class AffectedChangesDetectorFactory implements TestExecutionPlannerFactory {

    @Override
    public String alias() {
        return "affected";
    }

    @Override
    public boolean isFor(String name) {
        return alias().equalsIgnoreCase(name);
    }

    @Override
    public TestExecutionPlanner create(File projectDir, String[] globPatterns) {
        // TODO logic of inspecting git changes should be some where common so it is not recalculated several times
        // TODO in fact there are at least two things to be put in a Context to be reused (git changes (main and test) and graph of dependencies between tests and main classes

        final String previousCommit = System.getProperty("git.previous.commit", "HEAD");
        final String commit = System.getProperty("git.commit", "HEAD");

        // For now we recalculate everything
        AllChangedFilesDetector allChangedFilesDetector = new AllChangedFilesDetector(projectDir, previousCommit, commit, "**/src/main/java/**/*.java");
        final Set<File> mainClasses = allChangedFilesDetector.getFiles();

        return new AffectedChangesDetector(new FileSystemTestClassDetector(projectDir), mainClasses);
    }

}
