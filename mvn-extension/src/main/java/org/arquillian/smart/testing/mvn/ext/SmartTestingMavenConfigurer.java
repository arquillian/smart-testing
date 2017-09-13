package org.arquillian.smart.testing.mvn.ext;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.hub.storage.LocalChangeStorage;
import org.arquillian.smart.testing.mvn.ext.dependencies.ExtensionVersion;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.codehaus.plexus.component.annotations.Component;

import static java.util.stream.StreamSupport.stream;
import static org.arquillian.smart.testing.mvn.ext.MavenPropertyResolver.isSkipTestExecutionSet;
import static org.arquillian.smart.testing.mvn.ext.MavenPropertyResolver.isSpecificTestClassSet;

@Component(role = AbstractMavenLifecycleParticipant.class,
    description = "Entry point to install and manage Smart-Testing extension. Takes care of adding needed dependencies and "
        + "configures it on the fly.",
    hint = "smart-testing")
class SmartTestingMavenConfigurer extends AbstractMavenLifecycleParticipant {

    private static final Logger logger = Logger.getLogger();

    private final ChangeStorage changeStorage = new LocalChangeStorage(".");

    private Configuration configuration;

    private Boolean skipExtensionInstallation;

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {

        configuration = Configuration.load();

        if (session.getRequest().getLoggingLevel() == 0) {
            logger.enableMavenDebugLogLevel(true);
        }

        logger.debug("Applied user properties: %s", session.getUserProperties());

        if (shouldSkipExtensionInstallation()) {
            logExtensionDisableReason();
            return;
        }

        if (configuration.areStrategiesDefined()) {
            configureExtension(session, configuration);
            calculateChanges();
        } else {
            logStrategiesNotDefined();
        }
    }

    private void logExtensionDisableReason() {
        String reason = "Not Defined";

        if (configuration.isDisabled()) {
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

        if (logger.isDebugLogLevelEnabled()) {
            logger.debug("Version: %s", ExtensionVersion.version().toString());
            session.getAllProjects().forEach(mavenProject ->
                ModifiedPomExporter.exportModifiedPom(mavenProject.getModel()));
        }

        if (configuration.areStrategiesDefined()) {
            changeStorage.purgeAll();
            if (isFailedStrategyUsed()) {
                SurefireReportStorage.purgeReports(session);
            }
        } else {
            logStrategiesNotDefined();
        }
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
        final MavenProjectConfigurator mavenProjectConfigurator = new MavenProjectConfigurator(configuration);
        session.getAllProjects().forEach(mavenProject -> {
            mavenProjectConfigurator.configureTestRunner(mavenProject.getModel());
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
            skipExtensionInstallation = configuration.isDisabled() || isSkipTestExecutionSet() || isSpecificTestClassSet();
        }
        return skipExtensionInstallation;
    }
}
