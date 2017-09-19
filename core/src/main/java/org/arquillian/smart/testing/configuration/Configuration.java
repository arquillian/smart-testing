package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.RunMode;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.yaml.snakeyaml.Yaml;

import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;

public class Configuration {

    private static final Logger logger = Logger.getLogger();

    public static final String DEFAULT_MODE = "selecting";
    public static final String DEFAULT_STRATEGIES = "";
    public static final String SMART_TESTING_REPORT_ENABLE = "smart.testing.report.enable";
    public static final String DEFAULT_REPORT_FILE_NAME = "smart-testing-report.xml";
    public static final String SMART_TESTING_REPORT_DIR = "smart.testing.report.dir";
    public static final String SMART_TESTING_REPORT_NAME = "smart.testing.report.name";

    public static final String SMART_TESTING = "smart.testing";
    public static final String SMART_TESTING_MODE = "smart.testing.mode";
    public static final String SMART_TESTING_APPLY_TO = "smart.testing.apply.to";
    public static final String SMART_TESTING_VERSION = "smart.testing.version";
    public static final String SMART_TESTING_DISABLE = "smart.testing.disable";
    public static final String SMART_TESTING_DEBUG = "smart.testing.debug";

    private String[] strategies = new String[0];
    private RunMode mode;
    private String applyTo;

    private boolean disable;
    private boolean debug;

    private ReportConfiguration reportConfiguration;
    private ScmConfiguration scmConfiguration;

    public String[] getStrategies() {
        return strategies;
    }

    public void setStrategies(String[] strategies) {
        this.strategies = strategies;
    }

    public RunMode getMode() {
        return mode;
    }

    public void setMode(RunMode mode) {
        this.mode = mode;
    }

    public String getApplyTo() {
        return applyTo;
    }

    public void setApplyTo(String applyTo) {
        this.applyTo = applyTo;
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public ReportConfiguration getReportConfiguration() {
        return reportConfiguration;
    }

    public void setReportConfiguration(ReportConfiguration reportConfiguration) {
        this.reportConfiguration = reportConfiguration;
    }

    public ScmConfiguration getScmConfiguration() {
        return scmConfiguration;
    }

    public void setScmConfiguration(ScmConfiguration scmConfiguration) {
        this.scmConfiguration = scmConfiguration;
    }

    public static Configuration load() {
        final Yaml yaml = new Yaml();
        Map<String, Object> yamlConfiguration = new LinkedHashMap<>();

        File parent = new File(".");
        final File[] files = parent.listFiles((dir, name) -> name.equals("smart-testing.yml") || name.equals("smart-testing.yaml"));

        if (files != null) {
            if (files.length == 0) {
                logger.info(
                    "Config file `smart-testing.yaml` OR `smart-testing.yml` is not found. Using system properties to load "
                        + "configuration for smart testing.");
            } else {
                try (InputStream io = Files.newInputStream(getConfigurationFilePath(files))) {
                    yamlConfiguration = (Map<String, Object>) yaml.load(io);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }

        return parseConfiguration(yamlConfiguration);
    }

    public static Configuration loadPrecalculated() {
        final File configFile = new LocalStorage(".").execution().file("smart-testing.yml").getFile();

        return loadConfigurationFromFile(configFile);
    }

    static Configuration loadConfigurationFromFile(File configFile) {
        try(FileReader fileReader = new FileReader(configFile)) {
            final Yaml yaml = new Yaml();
            return yaml.loadAs(fileReader, Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration from file " + configFile.getPath(), e);
        }
    }

    private static Path getConfigurationFilePath(File[] files) {
        if(files.length == 1) {
            final File configFile = files[0];
            logger.info("Using configuration from " + configFile.getName());
            return configFile.toPath();
        }

        logger.warn("Found multiple config files with supported names: smart-testing.yml, smart-testing.yaml");
        logger.warn("Using configuration from smart-testing.yml");

        return Arrays.stream(files)
            .filter(file -> file.getName().equals("smart-testing.yml"))
            .map(File::toPath)
            .findFirst()
            .get();

    }

    static Configuration parseConfiguration(Map<String, Object> yamlConfiguration) {
        final Configuration configuration = new Configuration();

        configuration.mode = RunMode.valueOf(overWriteSystemProperty(yamlConfiguration, "mode", SMART_TESTING_MODE, DEFAULT_MODE).toUpperCase());

        final String strategies = overWriteSystemProperty(yamlConfiguration, "strategies", SMART_TESTING, DEFAULT_STRATEGIES);

        if (containsAnyStrategy(strategies)) {
            configuration.strategies = strategies.split("\\s*,\\s*");
        }

        configuration.disable = Boolean.valueOf(overWriteSystemProperty(yamlConfiguration, "disable", SMART_TESTING_DISABLE, "false"));
        configuration.debug = Boolean.valueOf(overWriteSystemProperty(yamlConfiguration, "debug", SMART_TESTING_DEBUG, "false"));
        configuration.applyTo = overWriteSystemProperty(yamlConfiguration, "applyTo", SMART_TESTING_APPLY_TO, null);

        final Map<String, Object> reportConfig = (Map<String, Object>) yamlConfiguration.get("report");

        final ReportConfiguration reportConfiguration = new ReportConfiguration();
        reportConfiguration.setEnable(Boolean.valueOf(overWriteSystemProperty(reportConfig, "enable", SMART_TESTING_REPORT_ENABLE, "false")));
        reportConfiguration.setDir(overWriteSystemProperty(reportConfig, "dir", SMART_TESTING_REPORT_DIR, "target"));
        reportConfiguration.setName(overWriteSystemProperty(reportConfig, "name", SMART_TESTING_REPORT_NAME, DEFAULT_REPORT_FILE_NAME));

        configuration.reportConfiguration = reportConfiguration;

        final Map<String, Object> scmConfig = (Map<String, Object>) yamlConfiguration.get("scm");

        final String lastChanges = overWriteSystemProperty(scmConfig, "lastChanges", SCM_LAST_CHANGES, "0");
        final Map<String, Object> scmRange = scmConfig != null ? (Map<String, Object>) scmConfig.get("range") : null;

        final ScmConfiguration scmConfigurationConfiguration = new ScmConfiguration();
        scmConfigurationConfiguration.setHead(overWriteSystemProperty(scmRange, "withHead", SCM_RANGE_HEAD, HEAD));
        scmConfigurationConfiguration.setTail(overWriteSystemProperty(scmRange, "withTail",
            SCM_RANGE_TAIL, String.join("~", HEAD, lastChanges)));

        configuration.scmConfiguration = scmConfigurationConfiguration;

        return configuration;
    }

    private static String overWriteSystemProperty(Map<String, Object> yamlConfig, String key, String propertyName, String defaultValue) {
        if (System.getProperty(propertyName) != null) {
            return System.getProperty(propertyName);
        } else if (yamlConfig != null && yamlConfig.get(key) != null) {
            return String.valueOf(yamlConfig.get(key));
        } else {
            return defaultValue;
        }
    }

    private static boolean containsAnyStrategy(String strategies) {
        return !strategies.trim().isEmpty();
    }

    public boolean isSelectingMode() {
        return isModeSet() && RunMode.SELECTING == getMode();
    }

    public boolean isModeSet() {
        return this.mode != null;
    }

    public boolean areStrategiesDefined() {
        return strategies.length > 0;
    }

    public boolean isApplyToDefined() {
        return this.applyTo != null;
    }

    public static Builder builder() {
        return new Configuration.Builder();
    }

    public static class Builder {
        private String[] strategies;
        private RunMode mode;
        private String applyTo;
        private boolean disable;
        private boolean debug;
        private ReportConfiguration reportConfiguration;
        private ScmConfiguration scmConfiguration;

        public Builder strategies(String[] strategies) {
            this.strategies = strategies;
            return this;
        }

        public Builder mode(RunMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder applyTo(String applyTo) {
            this.applyTo = applyTo;
            return this;
        }

        public Builder disable(boolean disable) {
            this.disable = disable;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder reportConfiguration(ReportConfiguration reportConfiguration) {
            this.reportConfiguration = reportConfiguration;
            return this;
        }

        public Builder scmConfiguration(ScmConfiguration scmConfiguration) {
            this.scmConfiguration = scmConfiguration;
            return this;
        }

        public Configuration build() {

            final Configuration configuration = new Configuration();
                configuration.strategies = this.strategies;
                configuration.mode = this.mode;
                configuration.applyTo = this.applyTo;
                configuration.disable = this.disable;
                configuration.debug = this.debug;
                configuration.reportConfiguration = this.reportConfiguration;
                configuration.scmConfiguration = this.scmConfiguration;

            return configuration;
        }
    }
}
