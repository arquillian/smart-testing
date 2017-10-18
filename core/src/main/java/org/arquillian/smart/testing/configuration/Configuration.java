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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.arquillian.smart.testing.RunMode;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalStorageFileAction;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.yaml.snakeyaml.Yaml;

public class Configuration implements ConfigurationSection {

    private static final Logger logger = Log.getLogger();

    public static final String DEFAULT_MODE = "selecting";
    public static final String SMART_TESTING_REPORT_ENABLE = "smart.testing.report.enable";

    public static final String SMART_TESTING = "smart.testing";
    public static final String SMART_TESTING_MODE = "smart.testing.mode";
    public static final String SMART_TESTING_CUSTOM_STRATEGIES = "smart.testing.strategy";
    public static final String SMART_TESTING_CUSTOM_STRATEGIES_PATTERN = SMART_TESTING_CUSTOM_STRATEGIES + ".*";
    public static final String SMART_TESTING_APPLY_TO = "smart.testing.apply.to";
    public static final String SMART_TESTING_VERSION = "smart.testing.version";
    public static final String SMART_TESTING_DISABLE = "smart.testing.disable";
    public static final String SMART_TESTING_DEBUG = "smart.testing.debug";
    public static final String SMART_TESTING_AUTOCORRECT = "smart.testing.autocorrect";

    public static final String SMART_TESTING_YML = "smart-testing.yml";
    public static final String SMART_TESTING_YAML = "smart-testing.yaml";

    private String[] strategies = new String[0];
    private String[] customStrategies = new String[0];
    private RunMode mode;
    private String applyTo;

    private boolean disable;
    private boolean debug;
    private boolean autocorrect;

    private Report report;
    private Scm scm;

    public String[] getStrategies() {
        return strategies;
    }

    public void setStrategies(String... strategies) {
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

    public boolean isAutocorrect() {
        return autocorrect;
    }

    public void setAutocorrect(boolean autocorrect) {
        this.autocorrect = autocorrect;
    }

    public void setCustomStrategies(String[] customStrategies) {
        this.customStrategies = customStrategies;
    }

    public String[] getCustomStrategies() {
        return customStrategies;
    }

    public List<ConfigurationItem> registerConfigurationItems() {
        List<ConfigurationItem> configItems = new ArrayList<>();
        configItems.add(new ConfigurationItem("strategies", SMART_TESTING, new String[0]));
        configItems.add(new ConfigurationItem("mode", SMART_TESTING_MODE, RunMode.valueOf(DEFAULT_MODE.toUpperCase())));
        configItems.add(new ConfigurationItem("applyTo", SMART_TESTING_APPLY_TO));
        configItems.add(new ConfigurationItem("disable", SMART_TESTING_DISABLE, false));
        configItems.add(new ConfigurationItem("debug", SMART_TESTING_DEBUG, false));
        configItems.add(new ConfigurationItem("autocorrect", SMART_TESTING_AUTOCORRECT, false));
        configItems.add(new ConfigurationItem("customStrategies", SMART_TESTING_CUSTOM_STRATEGIES_PATTERN));
        return configItems;
    }

    public static Configuration load() {
        return load(Paths.get("").toAbsolutePath().toFile());
    }

    public static Configuration load(File projectDir) {
        final File[] files =
            projectDir.listFiles((dir, name) -> name.equals(SMART_TESTING_YML) || name.equals(SMART_TESTING_YAML));

        Map<String, Object> yamlConfiguration = new LinkedHashMap<>();

        if (files == null) {
            throw new RuntimeException("I/O errors occurs while listing dir " + projectDir);
        }

        if (files.length == 0) {
            logger.info("Config file `" + SMART_TESTING_YAML + "` OR `" + SMART_TESTING_YML + "` is not found. "
                + "Using system properties to load configuration for smart testing.");
        } else {
            try (InputStream io = Files.newInputStream(getConfigurationFilePath(files))) {
                final Yaml yaml = new Yaml();
                yamlConfiguration = (Map<String, Object>) yaml.load(io);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return parseConfiguration(yamlConfiguration);
    }

    public static Configuration loadPrecalculated(File projectDir) {
        final File configFile =
            new LocalStorage(projectDir).duringExecution().temporary().file(SMART_TESTING_YML).getFile();
        if (configFile.exists()) {
            return loadConfigurationFromFile(configFile);
        } else {
            return load(projectDir);
        }
    }

    static Configuration loadConfigurationFromFile(File configFile) {
        try (FileReader fileReader = new FileReader(configFile)) {
            final Yaml yaml = new Yaml();
            return yaml.loadAs(fileReader, Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration from file " + configFile, e);
        }
    }

    public File dump(File rootDir) {
        final LocalStorageFileAction configFile = new LocalStorage(rootDir)
            .duringExecution()
            .temporary()
            .file(Configuration.SMART_TESTING_YML);
        try {
            configFile.create();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create file " + configFile, e);
        }

        try (FileWriter fileWriter = new FileWriter(configFile.getFile())) {
            Yaml yaml = new Yaml();
            yaml.dump(this, fileWriter);

            return configFile.getFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store configuration in file " + configFile, e);
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
        if (files.length == 1) {
            final File configFile = files[0];
            logger.info("Using configuration from " + configFile.getName());
            return configFile.toPath();
        }

        logger.warn("Found multiple config files with supported names: " + SMART_TESTING_YAML + ", " + SMART_TESTING_YML);
        logger.warn("Using configuration from " + SMART_TESTING_YML);

        return Arrays.stream(files)
            .filter(file -> file.getName().equals(SMART_TESTING_YML))
            .map(File::toPath)
            .findFirst()
            .get();
    }

    private static Configuration parseConfiguration(Map<String, Object> yamlConfiguration) {
        return ObjectMapper.mapToObject(Configuration.class, yamlConfiguration);
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
}
