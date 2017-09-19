package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalChangeStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalStorageFileAction;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.mvn.ext.dependencies.ExtensionVersion;
import org.arquillian.smart.testing.mvn.ext.logger.MavenExtensionLoggerFactory;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import static java.util.stream.StreamSupport.stream;
import static org.arquillian.smart.testing.mvn.ext.MavenPropertyResolver.isSkipTestExecutionSet;
import static org.arquillian.smart.testing.mvn.ext.MavenPropertyResolver.isSpecificTestClassSet;

@Component(role = AbstractMavenLifecycleParticipant.class,
    description = "Entry point to install and manage Smart-Testing extension. Takes care of adding needed dependencies and "
        + "configures it on the fly.",
    hint = "smart-testing")
class SmartTestingMavenConfigurer extends AbstractMavenLifecycleParticipant {

    private Logger logger;

    @Requirement
    public org.codehaus.plexus.logging.Logger mavenLogger;

    private final ChangeStorage changeStorage = new LocalChangeStorage(".");

    private Configuration configuration;

    private Boolean skipExtensionInstallation;

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        configuration = Configuration.load();

        Log.setLoggerFactory(new MavenExtensionLoggerFactory(mavenLogger, configuration));
        logger = Log.getLogger();

        logger.debug("Applied user properties: %s", session.getUserProperties());

        if (shouldSkipExtensionInstallation()) {
            logExtensionDisableReason();
            return;
        }

        if (configuration.areStrategiesDefined()) {
            configureExtension(session, configuration);
            calculateChanges();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> purgeLocalStorage(session)));
        } else {
            logStrategiesNotDefined();
        }
    }

    private void copyConfigurationFile(Model model, File parentFile) {
        final LocalStorageFileAction configFile = new LocalStorage(model.getProjectDirectory())
            .duringExecution()
            .temporary()
            .file(Configuration.SMART_TESTING_YML);
        logger.debug("Copying " + Configuration.SMART_TESTING_YML + " from [%s] to [%s]", parentFile.getPath(),
            configFile.getPath());

        try {
            configFile.create(Files.readAllBytes(parentFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void logExtensionDisableReason() {
        String reason = "Not Defined";

        if (configuration.isDisable()) {
            reason = "System Property `SMART_TESTING_DISABLE` is set.";
        } else if (isSkipTestExecutionSet()) {
            reason = "Test Execution has been skipped.";
        } else if (isSpecificTestClassSet()) {
            reason = "Single Test Class execution is set.";
        }

        logger.info("Smart Testing is disabled. Reason: %s", reason);
    }

    @Override
    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
        if (skipExtensionInstallation) {
            return;
        }

        if (configuration.isDebug() || mavenLogger.isDebugEnabled()) {
            logger.debug("Version: %s", ExtensionVersion.version().toString());
            session.getAllProjects().forEach(mavenProject ->
                ModifiedPomExporter.exportModifiedPom(mavenProject.getModel()));
        }

        if (!configuration.areStrategiesDefined()) {
            logStrategiesNotDefined();
        }

        purgeLocalStorage(session);
    }

    private void calculateChanges() {
        final Iterable<ChangeResolver> changeResolvers =
            new JavaSPILoader().all(ChangeResolver.class, ChangeResolver::isApplicable);
        final Collection<Change> changes = stream(changeResolvers.spliterator(), false).map(ChangeResolver::diff)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        changeStorage.store(changes);
    }

    private void configureExtension(MavenSession session, Configuration configuration) {
        logger.info("Enabling extension.");
        final MavenProjectConfigurator mavenProjectConfigurator = new MavenProjectConfigurator(configuration);
        final File dumpedConfigFile = configuration.dump(Paths.get("").toFile());
        session.getAllProjects().forEach(mavenProject -> {
            mavenProjectConfigurator.configureTestRunner(mavenProject.getModel());
            copyConfigurationFile(mavenProject.getModel(), dumpedConfigFile);
            if (isFailedStrategyUsed()) {
                SurefireReportStorage.copySurefireReports(mavenProject.getModel());
            }
        });
    }

    private boolean isFailedStrategyUsed(){
        return Arrays.asList(configuration.getStrategies()).contains("failed");
    }

    private void logStrategiesNotDefined() {
        logger.warn(
            "Smart Testing Extension is installed but no strategies are provided. It won't influence the way how your tests are executed. "
                + "For details on how to configure it head over to http://bit.ly/st-config");
    }

    private boolean shouldSkipExtensionInstallation() {
        if (skipExtensionInstallation == null) {
            skipExtensionInstallation = configuration.isDisable() || isSkipTestExecutionSet() || isSpecificTestClassSet();
        }
        return skipExtensionInstallation;
    }

    private void purgeLocalStorage(MavenSession session) {
        session.getAllProjects().forEach(mavenProject -> {
            Model model = mavenProject.getModel();
            String target = model.getBuild() != null ? model.getBuild().getDirectory() : null;
            new LocalStorage(model.getProjectDirectory()).duringExecution().purge(target);
        });
    }
}
