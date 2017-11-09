package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static java.lang.System.getProperty;
import static java.util.Arrays.stream;

public class BuildConfigurator {

    private static final int RANDOM_PORT = 0;

    private static final String MVN_DEBUG_AGENT = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=%s,address=%s";
    private static final String SUREFIRE_DEBUG_SETTINGS = " -Xnoagent -Djava.compiler=NONE";
    private static final int DEFAULT_DEBUG_PORT = 8000;
    private static final int DEFAULT_SUREFIRE_DEBUG_PORT = 5005;

    private final ProjectBuilder projectBuilder;
    private final Map<String, String> systemProperties = new HashMap<>();
    private final Set<String> modulesToBeBuilt = new HashSet<>();

    private int remotePort = DEFAULT_DEBUG_PORT;
    private int surefireRemotePort = DEFAULT_SUREFIRE_DEBUG_PORT;
    private boolean quietMode = true;
    private boolean enableRemoteDebugging = false;
    private boolean suspend = true;
    private boolean mvnDebugOutput;
    private boolean enableSurefireRemoteDebugging = false;
    private boolean ignoreBuildFailure = false;
    private boolean skipTests = false;
    private File workingDirectory;
    private String mavenOpts = "-Xms512m -Xmx1024m";
    private String mavenVersion;
    private Using usingInstallation;
    private String executionDir;

    BuildConfigurator(ProjectBuilder projectBuilder) {
        systemProperties.put("surefire.exitTimeout", "-1"); // see http://bit.ly/2vARQ5p
        systemProperties.put("surefire.timeout", "0"); // see http://bit.ly/2u7xCAH
        this.projectBuilder = projectBuilder;
    }

    public ProjectBuilder configure() {
        return this.projectBuilder;
    }

    /**
     * Enables remote debugging of embedded maven run so we can troubleshoot our extension and provider.
     * By default it sets suspend to 'y', so the run will wait until we attach remote debugger to the port
     * DEFAULT_DEBUG_PORT
     */
    public BuildConfigurator withRemoteDebugging() {
        return withRemoteDebugging(DEFAULT_DEBUG_PORT, true);
    }

    /**
     * @param port if set to BuildConfigurator.RANDOM_PORT it will take random, free port
     */
    public BuildConfigurator withRemoteDebugging(int port) {
        return withRemoteDebugging(port, true);
    }

    /**
     *
     * @param port if set to BuildConfigurator.RANDOM_PORT it will take random, free port
     * @param suspend indicates if a process should wait for the debugger to attach
     */
    public BuildConfigurator withRemoteDebugging(int port, boolean suspend) {
        if (port == RANDOM_PORT) {
            port = getAvailableLocalPort();
        }
        this.enableRemoteDebugging = true;
        this.remotePort = port;
        this.suspend = suspend;
        return this;
    }

    /**
     * Enables remote debugging of surefire process, so we can troubleshoot our extension and provider.
     * By default it sets suspend to 'y', so the run will wait until we attach remote debugger to the port
     * DEFAULT_SUREFIRE_DEBUG_PORT
     */
    public BuildConfigurator withRemoteSurefireDebugging() {
        return withRemoteSurefireDebugging(DEFAULT_SUREFIRE_DEBUG_PORT, true);
    }

    /**
     * @param surefireRemotePort if set to BuildConfigurator.RANDOM_PORT it will take random, free port
     */
    public BuildConfigurator withRemoteSurefireDebugging(int surefireRemotePort) {
        return withRemoteSurefireDebugging(surefireRemotePort, true);
    }

    /**
     * @param surefireRemotePort if set to BuildConfigurator.RANDOM_PORT it will take random, free port
     * @param suspend indicates if a process should wait for the debugger to attach
     * @return
     */
    public BuildConfigurator withRemoteSurefireDebugging(int surefireRemotePort, boolean suspend) {
        if (surefireRemotePort == RANDOM_PORT) {
            surefireRemotePort = getAvailableLocalPort();
        }
        this.surefireRemotePort = surefireRemotePort;
        this.suspend = suspend;
        this.enableSurefireRemoteDebugging = true;
        return this;
    }

    public BuildConfigurator quiet(boolean quiet) {
        this.quietMode = quiet;
        return this;
    }

    public BuildConfigurator ignoreBuildFailure() {
        this.ignoreBuildFailure = true;
        return this;
    }

    public BuildConfigurator skipTests(boolean skipTests) {
        this.skipTests = skipTests;
        return this;
    }

    public BuildConfigurator logBuildOutput() {
        return logBuildOutput(true);
    }

    public BuildConfigurator logBuildOutput(boolean log) {
        return quiet(!log);
    }

    private BuildConfigurator withDebugOutput(boolean debug) {
        this.mvnDebugOutput = debug;
        quiet(!debug);
        return this;
    }

    /**
     * Enables mvn debug output (-X) flag. Implies build logging output.
     */
    public BuildConfigurator withDebugOutput() {
        return withDebugOutput(true);
    }

    public BuildConfigurator withSystemProperties(String... systemPropertiesPairs) {
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

    public BuildConfigurator withWorkingDirectory(String newWorkingDirectory) {
        this.workingDirectory = new File(newWorkingDirectory);
        return this;
    }

    public BuildConfigurator projects(String... projects) {
        this.modulesToBeBuilt.addAll(Arrays.asList(projects));
        return this;
    }

    public BuildConfigurator excludeProjects(String... projects) {
        this.modulesToBeBuilt.addAll(
            stream(projects)
                .map(excludeProject -> "!" + excludeProject)
                .collect(Collectors.toList())
        );
        return this;
    }

    public BuildConfigurator useMavenVersion(String mavenVersion) {
        this.mavenVersion = mavenVersion;
        return this;
    }

    public BuildConfigurator useSurefireVersion(String version) {
        File pomFile = new File(projectBuilder.getRoot().toAbsolutePath().toString()+ File.separator + "pom.xml");
        try {
            Model model = new MavenXpp3Reader().read(new FileInputStream(pomFile));
            model.getProperties().setProperty("version.surefire.plugin", version);
            new MavenXpp3Writer().write(new FileOutputStream(pomFile), model);
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException("Failed setting surefire version in pom.", e);
        }
        return this;
    }

    BuildConfigurator executionDir(String executionDir) {
        this.executionDir = executionDir;
        return this;
    }

    void enableDebugOptions() {
        if (isRemoteDebugEnabled()) {
            final String debugOptions = String.format(MVN_DEBUG_AGENT, shouldSuspend(), getRemotePort());
            addMavenOpts(debugOptions);
        }

        if (isSurefireRemoteDebuggingEnabled()) {
            this.systemProperties.put("maven.surefire.debug",
                String.format(MVN_DEBUG_AGENT, shouldSuspend(), getSurefireDebugPort()) + SUREFIRE_DEBUG_SETTINGS);
        }
    }

    void addMavenOpts(String options) {
        this.mavenOpts += " " + options;
    }

    boolean disableQuietWhenAnyDebugModeEnabled() {
        return !isSurefireRemoteDebuggingEnabled() && !isRemoteDebugEnabled();
    }

    boolean isMavenDebugOutputEnabled() {
        return Boolean.valueOf(getProperty("test.bed.mvn.debug.output", Boolean.toString(this.mvnDebugOutput)));
    }

    Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    String[] getModulesToBeBuilt() {
        return modulesToBeBuilt.toArray(new String[modulesToBeBuilt.size()]);
    }

    boolean isQuietMode() {
        return quietMode;
    }

    boolean isSkipTestsEnabled() {
        return skipTests;
    }

    String getMavenOpts() {
        return mavenOpts;
    }

    File getWorkingDirectory() {
        return workingDirectory;
    }

    String getExecutionDir() {
        return executionDir;
    }

    private boolean isRemoteDebugEnabled() {
        return Boolean.valueOf(getProperty("test.bed.mvn.remote.debug", Boolean.toString(this.enableRemoteDebugging)));
    }

    boolean isSurefireRemoteDebuggingEnabled() {
        return Boolean.valueOf(
            getProperty("test.bed.mvn.surefire.remote.debug", Boolean.toString(this.enableSurefireRemoteDebugging)));
    }

    private String shouldSuspend() {
        final Boolean suspend =
            Boolean.valueOf(getProperty("test.bed.mvn.remote.debug.suspend", Boolean.toString(this.suspend)));
        return suspend ? "y" : "n";
    }

    int getRemotePort() {
        return Integer.valueOf(getProperty("test.bed.mvn.remote.debug.port", Integer.toString(this.remotePort)));
    }

    int getSurefireDebugPort() {
        return Integer.valueOf(
            getProperty("test.bed.mvn.surefire.remote.debug.port", Integer.toString(this.surefireRemotePort)));
    }

    boolean isIgnoreBuildFailure() {
        return ignoreBuildFailure;
    }

    String getMavenVersion() {
        return mavenVersion;
    }

    private int getAvailableLocalPort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // whatever ... ;)
                }
            }
        }
    }

    Using getUsingInstallation() {
        return usingInstallation;
    }

    void setUsingInstallation(Using usingInstallation) {
        this.usingInstallation = usingInstallation;
    }

}
