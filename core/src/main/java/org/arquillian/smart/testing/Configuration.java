package org.arquillian.smart.testing;

public class Configuration {

    public static final String SMART_TESTING = "smart.testing";
    public static final String SMART_TESTING_MODE = "smart.testing.mode";
    public static final String SMART_TESTING_PLUGIN = "smart.testing.plugin";
    public static final String SMART_TESTING_VERSION = "smart.testing.version";

    private String[] strategies = new String[0];
    private RunMode mode;
    private String smartTestingPlugin;

    public static Configuration read() {
        final Configuration configuration = new Configuration();

        final String strategies = System.getProperty(SMART_TESTING, "").toLowerCase();

        if (containsAnyStrategy(strategies)) {
            configuration.strategies = strategies.split("\\s*,\\s*");
        }
        configuration.mode = RunMode.valueOf(System.getProperty(SMART_TESTING_MODE, "ordering").toUpperCase());

        configuration.smartTestingPlugin = System.getProperty(SMART_TESTING_PLUGIN);

        return configuration;
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

    public RunMode getMode() {
        return mode;
    }

    public String[] getStrategies() {
        return strategies;
    }

    public String getSmartTestingPlugin() {
        return smartTestingPlugin;
    }

    public boolean isSmartTestingPluginDefined() {
        return this.smartTestingPlugin != null;
    }
}
