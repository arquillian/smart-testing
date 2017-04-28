package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.strategies.affected.AffectedChangesDetector;
import org.arquillian.smart.testing.vcs.git.ChangedFilesDetector;
import org.arquillian.smart.testing.vcs.git.NewFilesDetector;
import static java.io.File.pathSeparator;


public class TestStrategyApplier {

    private TestsToRun testsToRun;
    ProviderParametersParser paramParser;

    public TestStrategyApplier(TestsToRun testsToRun, ProviderParametersParser paramParser) {
        this.testsToRun = testsToRun;
        this.paramParser = paramParser;
    }

    public TestsToRun apply(List<String> orderStrategy) {
        // here I should call the planner implementations using getPlannerForStrategy method
        final Set<Class<?>> orderedTests = new LinkedHashSet<>();
        for (final String strategy : orderStrategy) {

            final TestExecutionPlanner plannerForStrategy = getPlannerForStrategy(strategy);
            final List<? extends Class<?>> tests = plannerForStrategy.getTests().stream().map(testClass -> {
                try {
                    return Class.forName(testClass);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }).collect(Collectors.toList());
            orderedTests.addAll(tests);
        }
        testsToRun = new TestsToRun(orderedTests);
        return testsToRun;
    }

    // TODO inverse creation of it - shouldn't belong to mvn package
    private TestExecutionPlanner getPlannerForStrategy(String orderStrategy) {
        final File projectDir = new File(System.getProperty("user.dir"));
        final String previousCommit = System.getProperty("git.previous.commit", "HEAD");
        final String commit = System.getProperty("git.commit", "HEAD");

        final String[] globPatternsAsArray = getGlobPatterns();

        if (Objects.equals(orderStrategy, "new")) {
            return new NewFilesDetector(projectDir, previousCommit, commit, globPatternsAsArray);
        } else if (Objects.equals(orderStrategy, "changed")) {
            return new ChangedFilesDetector(projectDir, previousCommit, commit, globPatternsAsArray);
        } else if (Objects.equals(orderStrategy, "affected")) {
            // TODO logic of inspecting git changes should be some where common so it is not recalculated several times
            // TODO in fact there are at least two things to be put in a Context to be reused (git changes (main and test) and graph of dependencies between tests and main classes

            // For now we recalculate everything
            // TODO now they are not tests they are main classes
            final Set<File> mainClasses =
                new NewFilesDetector(projectDir, previousCommit, commit, "**/src/main/java/**/*.java").getFiles();

            mainClasses.addAll(
                new ChangedFilesDetector(projectDir, previousCommit, commit, "**/src/main/java/**/*.java").getFiles());

            return new AffectedChangesDetector(projectDir, mainClasses);
        }

        return Collections::emptyList;
    }

    private String[] getGlobPatterns() {
        final List<String> globPatterns = paramParser.getIncludes();
        globPatterns.addAll(paramParser.getExcludes());
        return globPatterns.toArray(new String[globPatterns.size()]);
    }
}
