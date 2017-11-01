package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;
import org.arquillian.smart.testing.RunMode;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalStorageFileAction;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.StrategyConfiguration;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;
import org.yaml.snakeyaml.Yaml;

import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;
import static org.arquillian.smart.testing.configuration.ObjectMapper.mapToObject;

public class Configuration implements ConfigurationSection {

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

    private String[] strategies = new String[0];
    private String[] customStrategies = new String[0];
    private RunMode mode;
    private String applyTo;

    private boolean disable;
    private boolean debug;
    private boolean autocorrect;

    private Report report;
    private Scm scm;

    private Map<String, Object> strategiesConfig = new HashMap<>();

    private List<StrategyConfiguration> strategiesConfiguration = new ArrayList<>();

    @SuppressWarnings("unused") // Used to map YAML data to Java Objects in snakeYAML
    public List<StrategyConfiguration> getStrategiesConfiguration() {
        return strategiesConfiguration;
    }

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

    public void setStrategiesConfiguration(List<StrategyConfiguration> strategiesConfiguration) {
        this.strategiesConfiguration = strategiesConfiguration;
    }

    public void setStrategiesConfig(Map<String, Object> strategiesConfig) {
        this.strategiesConfig = strategiesConfig;
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

    public void loadStrategyConfigurations(String... strategies) {
        this.strategiesConfiguration = getStrategiesConfigurations(strategies);
    }

    private List<StrategyConfiguration> getStrategiesConfigurations(String... strategies) {
        List<StrategyConfiguration> convertedList = new ArrayList<>();

        StreamSupport.stream(new JavaSPILoader().all(TestExecutionPlannerFactory.class).spliterator(), false)
            .filter(
                testExecutionPlannerFactory -> Arrays.asList(strategies).contains(testExecutionPlannerFactory.alias()))
            .map(TestExecutionPlannerFactory::strategyConfiguration)
            .filter(Objects::nonNull)
            .forEach(strategyConfiguration -> {
                final Class<StrategyConfiguration> strategyConfigurationClass =
                    (Class<StrategyConfiguration>) strategyConfiguration.getClass();
                final Object strategyConfig = strategiesConfig.get(strategyConfiguration.name());
                Map<String, Object> strategyConfigMap = new HashMap<>();
                if (strategyConfig != null) {
                    strategyConfigMap = (Map<String, Object>) strategyConfig;
                }
                convertedList.add(mapToObject(strategyConfigurationClass, strategyConfigMap));
            });

        return convertedList;
    }

    public File dump(File rootDir) {
        final LocalStorageFileAction configFile = new LocalStorage(rootDir)
            .duringExecution()
            .temporary()
            .file(SMART_TESTING_YML);
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

    public StrategyConfiguration getStrategyConfiguration(String strategyName) {
        return this.strategiesConfiguration.stream()
            .filter(strategyConfiguration -> strategyName.equals(strategyConfiguration.name()))
            .findFirst()
            .orElseGet(() ->
                getStrategiesConfigurations(strategyName)
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                        new RuntimeException("The configuration class of strategy " + strategyName
                            + " is not available. Make sure you have correct dependencies on you classpath.")));
    }
}
