package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.RunMode;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalStorageFileAction;
import org.yaml.snakeyaml.Yaml;

import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.DEFAULT_LAST_COMMITS;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;

public class Configuration {

    private static final Logger logger = Logger.getLogger();

    public static final String DEFAULT_MODE = "selecting";
    public static final String DEFAULT_STRATEGIES = "";
    public static final String SMART_TESTING_REPORT_ENABLE = "smart.testing.report.enable";

    public static final String SMART_TESTING = "smart.testing";
    public static final String SMART_TESTING_MODE = "smart.testing.mode";
    public static final String SMART_TESTING_APPLY_TO = "smart.testing.apply.to";
    public static final String SMART_TESTING_VERSION = "smart.testing.version";
    public static final String SMART_TESTING_DISABLE = "smart.testing.disable";
    public static final String SMART_TESTING_DEBUG = "smart.testing.debug";

    public static final String SMART_TESTING_YML = "smart-testing.yml";
    public static final String SMART_TESTING_YAML = "smart-testing.yaml";

    private String[] strategies = new String[0];
    private RunMode mode;
    private String applyTo;

    private boolean disable;
    private boolean debug;

    private Report report;
    private Scm scm;

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

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Scm getScm() {
        return scm;
    }

    public void setScm(Scm scm) {
        this.scm = scm;
    }

    public static Configuration load() {
        final File parent = Paths.get("").toAbsolutePath().toFile();
        final File[] files = parent.listFiles((dir, name) -> name.equals(SMART_TESTING_YML) || name.equals(SMART_TESTING_YAML));

        Map<String, Object> yamlConfiguration = new LinkedHashMap<>();

        if (files != null) {
            if (files.length == 0) {
                logger.info(
                    "Config file `" + SMART_TESTING_YAML + "` OR `" + SMART_TESTING_YAML + "` is not found. Using system properties to load "
                        + "configuration for smart testing.");
            } else {
                try (InputStream io = Files.newInputStream(getConfigurationFilePath(files))) {
                    final Yaml yaml = new Yaml();
                    yamlConfiguration = (Map<String, Object>) yaml.load(io);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }

        return parseConfiguration(yamlConfiguration);
    }

    public static Configuration loadPrecalculated() {
        final File parent = Paths.get("").toAbsolutePath().toFile();
        final File configFile = new LocalStorage(parent).duringExecution().temporary().file(SMART_TESTING_YML).getFile();

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

    public Path dump() {
        final LocalStorageFileAction configFile = new LocalStorage(Paths.get("").toFile())
            .duringExecution()
            .temporary()
            .file(Configuration.SMART_TESTING_YML);
        try {
            configFile.create();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create " + configFile.getPath() + " file", e);
        }

        try (FileWriter fileWriter = new FileWriter(configFile.getFile())) {
            Yaml yaml = new Yaml();
            yaml.dump(this, fileWriter);

            return configFile.getPath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store configuration in file " + configFile.getPath(), e);
        }
    }

    // testing
    public static Configuration load(Path path) {
        try (InputStream io = Files.newInputStream(path)) {
            final Yaml yaml = new Yaml();
            Map<String, Object> yamlConfiguration = (Map<String, Object>) yaml.load(io);
            return parseConfiguration(yamlConfiguration);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path getConfigurationFilePath(File[] files) {
        if(files.length == 1) {
            final File configFile = files[0];
            logger.info("Using configuration from " + configFile.getName());
            return configFile.toPath();
        }

        logger.warn("Found multiple config files with supported names: " + SMART_TESTING_YAML + ", "  + SMART_TESTING_YML);
        logger.warn("Using configuration from " + SMART_TESTING_YML);

        return Arrays.stream(files)
            .filter(file -> file.getName().equals(SMART_TESTING_YML))
            .map(File::toPath)
            .findFirst()
            .get();
    }

    private static Configuration parseConfiguration(Map<String, Object> yamlConfiguration) {
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

        final Report report = new Report();
        report.setEnable(Boolean.valueOf(overWriteSystemProperty(reportConfig, "enable", SMART_TESTING_REPORT_ENABLE, "false")));
        report.setDir("target");
        report.setName(REPORT_FILE_NAME);

        configuration.report = report;

        final Map<String, Object> scmConfig = (Map<String, Object>) yamlConfiguration.get("scm");

        final String lastChanges = overWriteSystemProperty(scmConfig, "lastChanges", SCM_LAST_CHANGES, DEFAULT_LAST_COMMITS);

        final Map<String, Object> scmRange = scmConfig != null ? (Map<String, Object>) scmConfig.get("range") : null;
        final Range range = Range.builder()
                .head(overWriteSystemProperty(scmRange, "head", SCM_RANGE_HEAD, HEAD))
                .tail(overWriteSystemProperty(scmRange, "tail", SCM_RANGE_TAIL, String.join("~", HEAD, lastChanges)))
            .build();

        configuration.scm = Scm.builder().range(range).build();

        return configuration;
    }

    private static String overWriteSystemProperty(Map<String, Object> yamlConfig, String key, String propertyName, String defaultValue) {
        if (System.getProperty(propertyName) != null) {
            return System.getProperty(propertyName);
        } else if (yamlConfig != null && yamlConfig.get(key) != null) {
            final Object value = yamlConfig.get(key);
            if (value instanceof List) {
                final List<String> list = (List<String>) value;
                return String.join(",", list);
            }
            return String.valueOf(value);
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
        private Report report;
        private Scm scm;

        public Builder strategies(String... strategies) {
            this.strategies = strategies;
            return this;
        }

        public Builder mode(RunMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder mode(String mode) {
            mode(RunMode.valueOf(mode.toUpperCase()));
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

        public Builder report(Report report) {
            this.report = report;
            return this;
        }

        public Builder scm(Scm scm) {
            this.scm = scm;
            return this;
        }

        public Configuration build() {

            final Configuration configuration = new Configuration();
            configuration.strategies = this.strategies;
            configuration.mode = this.mode;
            configuration.applyTo = this.applyTo;
            configuration.disable = this.disable;
            configuration.debug = this.debug;
            configuration.report = this.report;
            configuration.scm = this.scm;

            return configuration;
        }
    }
}
