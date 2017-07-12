package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.PomEquippedEmbeddedMaven;

import static java.lang.System.getProperty;
import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.FAILURE;
import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.PASSED;
import static org.arquillian.smart.testing.ftest.testbed.testresults.SurefireReportReader.loadTestResults;

public class ProjectBuilder {

    private static final String TEST_REPORT_PREFIX = "TEST-";
    private static final String MVN_DEBUG_AGENT = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=%s,address=%s";
    private static final String SUREFIRE_DEBUG_SETTINGS = " -Xnoagent -Djava.compiler=NONE";
    private static final int DEFAULT_DEBUG_PORT = 8000;
    private static final int DEFAULT_SUREFIRE_DEBUG_PORT = 5005;

    private final Path root;
    private final Project project;
    private final Properties systemProperties = new Properties();

    private int remotePort = DEFAULT_DEBUG_PORT;
    private int surefireRemotePort = DEFAULT_SUREFIRE_DEBUG_PORT;
    private boolean quietMode = true;
    private boolean enableRemoteDebugging = false;
    private boolean suspend = true;
    private boolean mvnDebugOutput;
    private boolean enableSurefireRemoteDebugging = false;

    ProjectBuilder(Path root, Project project) {
        this.root = root;
        this.project = project;
    }

    public Project configure() {
        return this.project;
    }

    /**
     * Enables remote debugging of embedded maven build so we can troubleshoot our extension and provider
     * By default it sets suspend to 'y', so the build will wait until we attach remote debugger to the port DEFAULT_DEBUG_PORT
     */
    public ProjectBuilder withRemoteDebugging() {
        return withRemoteDebugging(DEFAULT_DEBUG_PORT, true);
    }

    public ProjectBuilder withRemoteDebugging(int port) {
        return withRemoteDebugging(port, true);
    }

    public ProjectBuilder withRemoteDebugging(int port, boolean suspend) {
        this.enableRemoteDebugging = true;
        this.remotePort = port;
        this.suspend = suspend;
        return this;
    }

    public ProjectBuilder withRemoteSurefireDebugging() {
        return withRemoteSurefireDebugging(DEFAULT_SUREFIRE_DEBUG_PORT, true);
    }

    public ProjectBuilder withRemoteSurefireDebugging(int surefireRemotePort) {
        return withRemoteSurefireDebugging(surefireRemotePort, true);
    }

    public ProjectBuilder withRemoteSurefireDebugging(int surefireRemotePort, boolean suspend) {
        this.surefireRemotePort = surefireRemotePort;
        this.suspend = suspend;
        this.enableSurefireRemoteDebugging = true;
        return this;
    }

    public ProjectBuilder quiet(boolean quiet) {
        this.quietMode = quiet;
        return this;
    }

    /**
     * Enables mvn debug output (-X) flag.
     */
    public ProjectBuilder withDebugOutput() {
        this.mvnDebugOutput = true;
        quiet(false);
        return this;
    }

    public ProjectBuilder withSystemProperties(String ... systemPropertiesPairs) {
        if (systemPropertiesPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Expecting even amount of variable name - value pairs to be passed. Got "
                + systemPropertiesPairs.length
                + " entries. "
                + Arrays.toString(systemPropertiesPairs));
        }

        for (int i = 0; i < systemPropertiesPairs.length; i += 2) {
            this.systemProperties.put(systemPropertiesPairs[i], systemPropertiesPairs[i + 1]);
        }

        return this;
    }

    List<TestResult> build(String... goals) {
        final PomEquippedEmbeddedMaven embeddedMaven =
            EmbeddedMaven.forProject(root.toAbsolutePath().toString() + "/pom.xml");

        enableDebugOptions(embeddedMaven);

        final BuiltProject build = embeddedMaven
                    .setGoals(goals)
                    .setDebug(isMavenDebugOutputEnabled())
                    .setQuiet(!isMavenDebugOutputEnabled() && quietMode)
                    .skipTests(false)
                    .setProperties(systemProperties)
                    .ignoreFailure()
                .build();

        if (build.getMavenBuildExitCode() != 0) {
            System.out.println(build.getMavenLog());
            throw new IllegalStateException("Maven build has failed, see logs for details");
        }

        return accumulatedTestResults();
    }

    private void enableDebugOptions(PomEquippedEmbeddedMaven embeddedMaven) {
        if (isRemoteDebugEnabled()) {
            final String debugOptions = String.format(MVN_DEBUG_AGENT, shouldSuspend(), getRemotePort());
            System.out.println(">>> Executing build with debug options: " + debugOptions);
            embeddedMaven.setMavenOpts(debugOptions);
        }

        if (isSurefireRemoteDebuggingEnabled()) {
            this.systemProperties.put("maven.surefire.debug",
                String.format(MVN_DEBUG_AGENT, shouldSuspend(), getSurefireDebugPort()) + SUREFIRE_DEBUG_SETTINGS);
        }
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

    private boolean isRemoteDebugEnabled() {
        return Boolean.valueOf(getProperty("test.bed.mvn.remote.debug", Boolean.toString(this.enableRemoteDebugging)));
    }

    boolean isSurefireRemoteDebuggingEnabled() {
        return Boolean.valueOf(getProperty("test.bed.mvn.surefire.remote.debug", Boolean.toString(this.enableSurefireRemoteDebugging)));
    }

    private String shouldSuspend() {
        final Boolean suspend =
            Boolean.valueOf(getProperty("test.bed.mvn.remote.debug.suspend", Boolean.toString(this.suspend)));
        return suspend ? "y" : "n";
    }

    int getRemotePort() {
        return Integer.valueOf(getProperty("test.bed.mvn.remote.debug.port", Integer.toString(this.remotePort)));
    }

    boolean isMavenDebugOutputEnabled() {
        return Boolean.valueOf(getProperty("test.bed.mvn.debug.output", Boolean.toString(this.mvnDebugOutput)));
    }

    int getSurefireDebugPort() {
        return Integer.valueOf(
            getProperty("test.bed.mvn.surefire.remote.debug.port", Integer.toString(this.surefireRemotePort)));
    }
}
