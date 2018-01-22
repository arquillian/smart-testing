package org.arquillian.smart.testing.ftest.testbed.project;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.PomEquippedEmbeddedMaven;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.toMap;
import static org.arquillian.smart.testing.ftest.testbed.testresults.SurefireReportReader.loadTestResults;

public class ProjectBuilder {

    public static final String TEST_REPORT_PREFIX = "TEST-";
    private static final String POM_XML = "pom.xml";

    private static final Logger LOGGER = Log.getLogger();

    private final Path root;
    private final BuildConfigurator buildConfigurator;
    private BuiltProject builtProject;

    ProjectBuilder(Path root) {
        this.root = root;
        this.buildConfigurator = new BuildConfigurator(this);
    }

    public BuildConfigurator options() {
        return this.buildConfigurator;
    }

    public TestResults run() {
        return run("clean", "package");
    }
    public TestResults run(String... goals) {
        builtProject = executeGoals(goals);
        return accumulatedTestResults();
    }

    Path getRoot() {
        return root;
    }

    private BuiltProject executeGoals(String... goals) {
        final String executionDir = buildConfigurator.getExecutionDir();
        final String rootPath = root.toAbsolutePath().toString();
        Path pomFile = executionDir != null ? Paths.get(rootPath, executionDir, POM_XML)
            : Paths.get(rootPath, POM_XML);
        final PomEquippedEmbeddedMaven embeddedMaven = EmbeddedMaven.forProject(pomFile.toString());

        embeddedMaven.setDebugLoggerLevel();
        buildConfigurator.enableDebugOptions();
        setCustomMavenInstallation(embeddedMaven);

        final BuiltProject build = embeddedMaven
                    .setShowVersion(true)
                    .setGoals(goals)
                    .setProjects(buildConfigurator.getModulesToBeBuilt())
                    .setDebug(buildConfigurator.isMavenDebugOutputEnabled())
                    .setQuiet(buildConfigurator.disableQuietWhenAnyDebugModeEnabled() && buildConfigurator.isQuietMode())
                    .setProperties(asProperties(buildConfigurator.getSystemProperties()))
                    .skipTests(buildConfigurator.isSkipTestsEnabled())
                    .setMavenOpts(buildConfigurator.getMavenOpts())
                    .setWorkingDirectory(buildConfigurator.getWorkingDirectory())
                    .setAlsoMake(true)
                    .ignoreFailure()
                .build();

        if (!buildConfigurator.isIgnoreBuildFailure() && build.getMavenBuildExitCode() != 0) {
            if (build.getMavenLog().contains("No tests were executed!")) {
                LOGGER.info("No tests were executed!");
            } else {
                System.out.println(build.getMavenLog());
                throw new IllegalStateException("Maven build has failed, see logs for details");
            }
        }

        return build;
    }

    String getMavenLog() {
        return builtProject.getMavenLog();
    }

    public BuiltProject getBuiltProject() {
        return builtProject;
    }

    private Properties asProperties(Map<String, String> propertyMap) {
        final Properties properties = new Properties();
        properties.putAll(propertyMap);
        return properties;
    }

    private void setCustomMavenInstallation(PomEquippedEmbeddedMaven embeddedMaven) {
        if (buildConfigurator.getMavenVersion() != null && !buildConfigurator.getMavenVersion().isEmpty()) {
            setMavenVersion(embeddedMaven);
        } else {
            final String mvnInstallation = System.getenv("TEST_BED_M2_HOME");
            if (mvnInstallation != null) {
                embeddedMaven.useInstallation(new File(mvnInstallation));
            } else {
                embeddedMaven.useDefaultDistribution();
            }
        }
    }

    private void setMavenVersion(PomEquippedEmbeddedMaven embeddedMaven) {
        String mavenVersion = buildConfigurator.getMavenVersion();
        embeddedMaven.useMaven3Version(mavenVersion);
        if (buildConfigurator.getUsingInstallation() == Using.SHADED_JAR) {
            String stVersion = buildConfigurator.getSystemProperties().get(Configuration.SMART_TESTING_VERSION);
            new MavenExtensionShadedJarRegisterer().addSmartTestingExtension(stVersion, mavenVersion);
        }
    }

    private TestResults accumulatedTestResults() {
        try {
            final List<TestResult> collect = Files.walk(root)
                .filter(this::isSurefireReportFile)
                .map(path -> {
                    try {
                        return Lists.newArrayList(loadTestResults(new FileInputStream(path.toFile())));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
            return new TestResults(collect);
        } catch (IOException e) {
            throw new RuntimeException("Failed extracting test results", e);
        }
    }

    private boolean isSurefireReportFile(Path path) {
        return !path.toFile().getAbsolutePath()
            .contains(File.separator + TemporaryInternalFiles.getTestReportDirName()) && path.getFileName()
            .toString().startsWith(TEST_REPORT_PREFIX);
    }
}
