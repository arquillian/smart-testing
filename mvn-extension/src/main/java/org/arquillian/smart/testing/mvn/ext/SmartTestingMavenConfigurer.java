package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalChangeStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.mvn.ext.dependencies.ExtensionVersion;
import org.arquillian.smart.testing.mvn.ext.logger.MavenExtensionLoggerFactory;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import static java.util.stream.StreamSupport.stream;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_DISABLE;

@Component(role = AbstractMavenLifecycleParticipant.class,
    description = "Entry point to install and manage Smart-Testing extension. Takes care of adding needed dependencies and "
        + "configures it on the fly.",
    hint = "smart-testing")
class SmartTestingMavenConfigurer extends AbstractMavenLifecycleParticipant {

    private Logger logger;

    @Requirement
    public org.codehaus.plexus.logging.Logger mavenLogger;

    private final ChangeStorage changeStorage = new LocalChangeStorage();

    private Configuration configuration;

    private boolean skipExtensionInstallation;

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        Log.setLoggerFactory(new MavenExtensionLoggerFactory(mavenLogger));

        loadConfigAndCheckIfInstallationShouldBeSkipped(session);
        if (skipExtensionInstallation) {
            return;
        }

        logger.debug("Version: %s", ExtensionVersion.version().toString());
        logger.debug("Applied user properties: %s", session.getUserProperties());

        File projectDirectory = session.getTopLevelProject().getModel().getProjectDirectory();
        if (configuration.areStrategiesDefined()) {
            logger.info("Enabling extension.");
            configureExtension(session, configuration);
            calculateChanges(projectDirectory, configuration);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> purgeLocalStorageAndExportPom(session)));
        } else {
            logStrategiesNotDefined();
        }
    }

    private void loadConfigAndCheckIfInstallationShouldBeSkipped(MavenSession session) {
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(session);
        skipExtensionInstallation = skipInstallationChecker.shouldSkip();
        if (skipExtensionInstallation) {
            logExtensionDisableReason(Log.getLogger(), skipInstallationChecker.getReason());
            return;
        }

        File projectDirectory = session.getTopLevelProject().getModel().getProjectDirectory();
        configuration = ConfigurationLoader.load(projectDirectory);
        Log.setLoggerFactory(new MavenExtensionLoggerFactory(mavenLogger, configuration));
        logger = Log.getLogger();

        if (configuration.isDisable()) {
            skipExtensionInstallation = true;
            logExtensionDisableReason(logger, "System Property " + SMART_TESTING_DISABLE + " is set.");
            return;
        }
    }

    private void logExtensionDisableReason(Logger customLogger, String customReason) {
        String reason = "Not Defined";
        if (customReason != null && !customReason.isEmpty()) {
            reason = customReason;
        }
        customLogger.info("Smart Testing is disabled. Reason: %s", reason);
    }

    @Override
    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {

        if (skipExtensionInstallation) {
            return;
        }

        if (!configuration.areStrategiesDefined()) {
            logStrategiesNotDefined();
        }

        purgeLocalStorageAndExportPom(session);
    }

    private void calculateChanges(File projectDirectory, Configuration configuration) {
        final Iterable<ChangeResolver> changeResolvers =
            new JavaSPILoader().all(ChangeResolver.class, resolver -> resolver.isApplicable(projectDirectory));
        final Collection<Change> changes = stream(changeResolvers.spliterator(), false)
            .map(changeResolver -> changeResolver.diff(projectDirectory, configuration))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        if (!changes.isEmpty()) {
            changeStorage.store(changes, projectDirectory);
        }
    }

    private void configureExtension(MavenSession session, Configuration configuration) {
        final MavenProjectConfigurator mavenProjectConfigurator = new MavenProjectConfigurator(configuration);
        session.getAllProjects().forEach(mavenProject -> {
            boolean wasConfigured = mavenProjectConfigurator.configureTestRunner(mavenProject.getModel());
            if (wasConfigured) {
                configuration.dump(mavenProject.getBasedir());
                if (isFailedStrategyUsed()) {
                    SurefireReportStorage.copySurefireReports(mavenProject.getModel());
                }
            }
        });
    }

    private boolean isFailedStrategyUsed() {
        return Arrays.asList(configuration.getStrategies()).contains("failed");
    }

    private void logStrategiesNotDefined() {
        logger.warn(
            "Smart Testing Extension is installed but no strategies are provided. It won't influence the way how your tests are executed. "
                + "For details on how to configure it head over to http://bit.ly/st-config");
    }

    private void purgeLocalStorageAndExportPom(MavenSession session) {
        session.getAllProjects().forEach(mavenProject -> {
            Model model = mavenProject.getModel();
            boolean isDebug = configuration.isDebug() || mavenLogger.isDebugEnabled();
            String target = model.getBuild() != null ? model.getBuild().getDirectory() : null;

            new LocalStorage(model.getProjectDirectory()).duringExecution().purge(target);
            if (isDebug && new File(target).exists()) {
                ModifiedPomExporter.exportModifiedPom(mavenProject.getModel());
            }
        });
    }
}
