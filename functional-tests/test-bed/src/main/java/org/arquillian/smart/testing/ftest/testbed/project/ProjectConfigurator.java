package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.RunMode;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.ftest.testbed.configuration.Mode;
import org.arquillian.smart.testing.ftest.testbed.configuration.Strategy;
import org.arquillian.smart.testing.ftest.testbed.configuration.builder.ConfigurationBuilder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

public class ProjectConfigurator {

    private static final String SMART_TESTING = "smart.testing";
    private static final String SMART_TESTING_MODE = "smart.testing.mode";
    private static final String SMART_TESTING_VERSION = "smart.testing.version";

    private Strategy[] strategies;
    private Mode mode;
    private String version;
    private Configuration configuration;

    private final Project project;
    private final Path root;
    private boolean createConfigFile;
    private Path configFilePath;

    ProjectConfigurator(Project project, Path root) {
        this.project = project;
        this.root = root;
    }

    Strategy[] getStrategies() {
        return strategies;
    }

    Mode getMode() {
        return mode;
    }

    public ProjectConfigurator executionOrder(Strategy... strategies) {
        this.strategies = strategies;
        return this;
    }

    public ProjectConfigurator withConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public ProjectConfigurator createConfigFile() {
        this.createConfigFile = true;
        createConfigurationFile(SMART_TESTING_YML);
        return this;
    }

    public ProjectConfigurator createConfigFileIn(String path) {
        return createConfigFile(path + File.separator + SMART_TESTING_YML);
    }

    public ProjectConfigurator createConfigFile(String configFile) {
        this.createConfigFile = true;
        createConfigurationFile(configFile);
        return this;
    }

    public ProjectConfigurator inMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public ProjectConfigurator version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Enables using extension file {@link Using#EXTENSION_FILE}
     *
     * @return A modified instance of {@link Project}
     */
    public Project enable() {
        return enable(Using.EXTENSION_FILE);
    }

    public Project enable(Using usingInstallation) {
        project.build().options().setUsingInstallation(usingInstallation);
        final Path rootPom = Paths.get(root.toString(), "pom.xml");
        String currentVersion = resolveVersion();
        if (usingInstallation == Using.EXTENSION_FILE) {
            new MavenExtensionFileRegisterer(rootPom).addSmartTestingExtension(currentVersion);
        }
        if (!createConfigFile) {
            this.project.build().options()
                .withSystemProperties(SMART_TESTING, strategies(), SMART_TESTING_MODE, getMode().getName(),
                    SMART_TESTING_VERSION, currentVersion)
                .configure();
        } else {
            if (configuration == null) {
                this.configuration = new ConfigurationBuilder()
                    .strategies(strategies().split("\\s*,\\s*"))
                    .mode(RunMode.valueOf(getMode().getName().toUpperCase()))
                    .build();
                dumpConfiguration(this.configFilePath);
            }
        }
        return this.project;
    }

    public String strategies() {
       return Arrays.stream(getStrategies()).map(Strategy::getName).collect(Collectors.joining(","));
    }

    private void createConfigurationFile(String configFilePath) {
        this.configFilePath = Paths.get(root.toString(), configFilePath);
        if (configuration != null) {
            dumpConfiguration(this.configFilePath);
        }
    }

    private void dumpConfiguration(Path configFilePath) {
        try (FileWriter fileWriter = new FileWriter(configFilePath.toFile())) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Representer representer = new Representer() {
                @Override
                protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,Tag customTag) {
                    // if value of property is null, ignore it.
                    if (propertyValue == null) {
                        return null;
                    }
                    else {
                        return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                    }
                }
            };

            representer.addClassTag(Configuration.class, Tag.MAP);

            Yaml yaml = new Yaml(representer, options);
            yaml.dump(configuration, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException("Failed to dump configuration in file " + configFilePath, e);
        }
    }

    private String resolveVersion() {

        if (this.version == null) {
            String systemProperty = System.getProperty(SMART_TESTING_VERSION);

            if (systemProperty == null) {
                return readVersionFromExtensionFile();
            }

            return systemProperty;
        }

        return this.version;
    }

    private String readVersionFromExtensionFile() {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(ProjectConfigurator.class.getResourceAsStream("/extension_version")))) {
            return reader.readLine().trim();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read extension version", e);
        }
    }
}
