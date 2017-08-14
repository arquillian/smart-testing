package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.PomEquippedEmbeddedMaven;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.toMap;
import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.FAILURE;
import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.PASSED;
import static org.arquillian.smart.testing.ftest.testbed.testresults.SurefireReportReader.loadTestResults;
import static org.arquillian.smart.testing.spi.TestResult.TEMP_REPORT_DIR;

public class ProjectBuilder {

    public static final String TEST_REPORT_PREFIX = "TEST-";

    private final Path root;
    private final BuildConfigurator buildConfigurator;

    ProjectBuilder(Path root) {
        this.root = root;
        this.buildConfigurator = new BuildConfigurator(this);
    }

    public BuildConfigurator options() {
        return this.buildConfigurator;
    }

    public List<TestResult> run() {
        return run("clean", "package");
    }

    public List<TestResult> run(String... goals) {
        return executeGoals(goals);
    }

    private List<TestResult> executeGoals(String... goals) {
        final PomEquippedEmbeddedMaven embeddedMaven =
            EmbeddedMaven.forProject(root.toAbsolutePath().toString() + "/pom.xml");

        buildConfigurator.enableDebugOptions();
        setCustomMavenInstallation(embeddedMaven);

        System.out.println("$ mvn " + Arrays.toString(goals).replaceAll("[\\[|\\]|,]", "") + " " + printSystemProperties());

        final BuiltProject build = embeddedMaven
                    .setShowVersion(true)
                    .setGoals(goals)
                    .setProjects(buildConfigurator.getModulesToBeBuilt())
                    .setDebug(buildConfigurator.isMavenDebugOutputEnabled())
                    .setQuiet(buildConfigurator.disableQuietWhenAnyDebugModeEnabled() && buildConfigurator.isQuietMode())
                    .setProperties(asProperties(buildConfigurator.getSystemProperties()))
                    .skipTests(buildConfigurator.isSkipTestsEnabled())
                    .setMavenOpts(buildConfigurator.getMavenOpts())
                    .ignoreFailure()
                .build();

        final String mavenLog = build.getMavenLog();

        if (buildConfigurator.isMavenLoggingEnabled()) {
                buildConfigurator.setMavenLog(mavenLog);
        }

        if (!buildConfigurator.isIgnoreBuildFailure() && build.getMavenBuildExitCode() != 0) {
            System.out.println(mavenLog);
            throw new IllegalStateException("Maven build has failed, see logs for details");
        }

        return accumulatedTestResults();
    }

    private Properties asProperties(Map<String, String> propertyMap) {
        final Properties properties = new Properties();
        properties.putAll(propertyMap);
        return properties;
    }

    private String printSystemProperties() {
        final StringBuilder sb = new StringBuilder();
        final Map<String, String> systemProperties = this.buildConfigurator.getSystemProperties()
            .entrySet()
            .stream()
            .sorted(comparingByKey())
            .collect(toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        systemProperties.forEach((key, value) -> sb.append("-D").append(key).append('=').append(value).append(" "));
        return sb.toString();
    }

    private void setCustomMavenInstallation(PomEquippedEmbeddedMaven embeddedMaven) {
        final String mvnInstallation = System.getenv("TEST_BED_M2_HOME");
        if (mvnInstallation != null) {
            embeddedMaven.useInstallation(new File(mvnInstallation));
        }
    }

    private List<TestResult> accumulatedTestResults() {
        try {
            return Files.walk(root)
                .filter(path -> !path.toFile().getAbsolutePath().contains(TEMP_REPORT_DIR) &&
                    path.getFileName().toString().startsWith(TEST_REPORT_PREFIX))
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
}
