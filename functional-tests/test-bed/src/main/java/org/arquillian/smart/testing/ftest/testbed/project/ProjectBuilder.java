package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.PomEquippedEmbeddedMaven;

import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.FAILURE;
import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.PASSED;
import static org.arquillian.smart.testing.ftest.testbed.testresults.SurefireReportReader.loadTestResults;

public class ProjectBuilder {

    private static final String TEST_REPORT_PREFIX = "TEST-";

    private final Path root;
    private final BuildConfigurator buildConfigurator;

    ProjectBuilder(Path root) {
        this.root = root;
        this.buildConfigurator = new BuildConfigurator(this);
    }

    public BuildConfigurator options() {
        return this.buildConfigurator;
    }

    List<TestResult> build(String... goals) {
        final PomEquippedEmbeddedMaven embeddedMaven =
            EmbeddedMaven.forProject(root.toAbsolutePath().toString() + "/pom.xml");

        buildConfigurator.enableDebugOptions(embeddedMaven);

        final String mvnInstallation = System.getenv("TEST_BED_M2_HOME");
        if (mvnInstallation != null) {
            embeddedMaven.useInstallation(new File(mvnInstallation));
        }

        final BuiltProject build = embeddedMaven
                    .setShowVersion(true)
                    .setGoals(goals)
                    .setProjects(buildConfigurator.getExcludedProjects())
                    .setDebug(buildConfigurator.isMavenDebugOutputEnabled())
                    .setQuiet(buildConfigurator.disableQuietWhenAnyDebugModeEnabled() && buildConfigurator.isQuietMode())
                    .skipTests(false)
                    .setProperties(buildConfigurator.getSystemProperties())
                    .setMavenOpts(buildConfigurator.getMavenOpts())
                    .ignoreFailure()
                .build();

        if (build.getMavenBuildExitCode() != 0) {
            System.out.println(build.getMavenLog());
            throw new IllegalStateException("Maven build has failed, see logs for details");
        }

        return accumulatedTestResults();
    }

    private List<TestResult> accumulatedTestResults() {
        try {
            return Files.walk(root)
                .filter(path -> path.getFileName().toString().startsWith(TEST_REPORT_PREFIX))
                .map(path -> {
                        try {
                            final Set<TestResult> testResults = loadTestResults(new FileInputStream(path.toFile()));
                            return testResults.stream()
                                .reduce(new TestResult("", "*", PASSED),
                                    (previous, current) -> new TestResult(current.getClassName(), "*",
                                        (previous.isFailing() || current.isFailing()) ? FAILURE : PASSED));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                )
                .distinct()
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed extracting test results", e);
        }
    }

    public List<TestResult> run() {
        return run("clean", "package");
    }

    public List<TestResult> run(boolean log) {
        if (log) {
            buildConfigurator.logBuildOutput();
        }
        return run("clean", "package");
    }

    public List<TestResult> run(String... goals) {
        return build(goals);
    }
}
