package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.arquillian.smart.testing.configuration.ConfigLookup;
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
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_CONFIG;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

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

        File executionRootDirectory = new File(session.getExecutionRootDirectory());
        configuration = ConfigurationLoader.load(executionRootDirectory, this::isProjectRootDirectory);
        Log.setLoggerFactory(new MavenExtensionLoggerFactory(mavenLogger, configuration));
        logger = Log.getLogger();

        if (configuration.isDisable()) {
            skipExtensionInstallation = true;
            logExtensionDisableReason(logger, SMART_TESTING_DISABLE + " is set.");
        }
    }

    private boolean isProjectRootDirectory(File file) {
        try {
            return file.isDirectory()
                && Files.isSameFile(file.toPath(), new File(System.getenv("MAVEN_PROJECTBASEDIR")).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        final Consumer<MavenProject> configureSmartTestingExtensionAction;
        final File executionRootDirectory = new File(session.getExecutionRootDirectory());
        if (hasModuleSpecificConfigurations(executionRootDirectory, this::isProjectRootDirectory)) {
            configureSmartTestingExtensionAction = applyModuleSpecificConfiguration(configuration);
        } else {
            final MavenProjectConfigurator mavenProjectConfigurator = new MavenProjectConfigurator(configuration);
            configureSmartTestingExtensionAction = mavenProject -> configureMavenProject(mavenProjectConfigurator, mavenProject, configuration);
        }
        session.getAllProjects().forEach(configureSmartTestingExtensionAction);
    }

    private boolean hasModuleSpecificConfigurations(File projectDir, Function<File, Boolean> stopCondition) {
        final ConfigLookup lookUp = new ConfigLookup(projectDir, stopCondition);
        final boolean moreThanOneConfigFile = lookUp.hasMoreThanOneConfigFile(SMART_TESTING_YML, SMART_TESTING_YAML);
        return moreThanOneConfigFile && System.getProperty(SMART_TESTING_CONFIG) == null;
    }

    private Consumer<MavenProject> applyModuleSpecificConfiguration(Configuration configuration) {
        return mavenProject -> {
            final ConfigLookup configLookup = new ConfigLookup(mavenProject.getBasedir(), this::isProjectRootDirectory);
            final File dirWithConfig = configLookup.getFirstDirWithConfigOrWithStopCondition();
            final Configuration mavenProjectConfiguration = configLookup.isConfigFromProjectRootDir() ? configuration :
                ConfigurationLoader.load(dirWithConfig);
            if (mavenProjectConfiguration.isDisable()) {
                logExtensionDisableReason(logger,
                    SMART_TESTING_DISABLE + " is set for module " + mavenProject.getArtifactId());
                return;
            }
            if (mavenProjectConfiguration.areStrategiesDefined()) {
                final MavenProjectConfigurator mavenProjectConfigurator =
                    new MavenProjectConfigurator(mavenProjectConfiguration);
                configureMavenProject(mavenProjectConfigurator, mavenProject, mavenProjectConfiguration);
            } else {
                logger.warn(
                    "Smart Testing Extension is installed but no strategies are provided for %s module. It won't influence the way how your tests are executed. "
                        + "For details on how to configure it head over to http://bit.ly/st-config",
                    mavenProject.getArtifactId());
            }
        };
    }

    private void configureMavenProject(MavenProjectConfigurator mavenProjectConfigurator, MavenProject mavenProject,
        Configuration configuration) {
        boolean wasConfigured = mavenProjectConfigurator.configureTestRunner(mavenProject.getModel());
        if (wasConfigured) {
            configuration.dump(mavenProject.getBasedir());
            if (isFailedStrategyUsed()) {
                SurefireReportStorage.copySurefireReports(mavenProject.getModel());
            }
        }
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
