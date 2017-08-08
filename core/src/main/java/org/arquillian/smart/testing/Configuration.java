package org.arquillian.smart.testing;

public class Configuration {

    public static final String DEFAULT_MODE = "selecting";
    public static final String DEFAULT_STRATEGIES = "";

    public static final String SMART_TESTING = "smart.testing";
    public static final String SMART_TESTING_MODE = "smart.testing.mode";
    public static final String SMART_TESTING_APPLY_TO = "smart.testing.apply.to";
    public static final String SMART_TESTING_VERSION = "smart.testing.version";
    public static final String SMART_TESTING_DISABLE = "smart.testing.disable";

    private String[] strategies = new String[0];
    private RunMode mode;
    private String applyTo;
    private boolean disabled = false;

    public static Configuration load() {
        final Configuration configuration = new Configuration();

        final String strategies = System.getProperty(SMART_TESTING, DEFAULT_STRATEGIES).toLowerCase();

        if (containsAnyStrategy(strategies)) {
            configuration.strategies = strategies.split("\\s*,\\s*");
        }
        configuration.mode = RunMode.valueOf(System.getProperty(SMART_TESTING_MODE, DEFAULT_MODE).toUpperCase());

        configuration.applyTo = System.getProperty(SMART_TESTING_APPLY_TO);

        configuration.disabled = Boolean.valueOf(System.getProperty(SMART_TESTING_DISABLE, "false"));

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

    public String getApplyTo() {
        return applyTo;
    }

    public boolean isApplyToDefined() {
        return this.applyTo != null;
    }

    public boolean isDisabled() {
        return disabled;
    }

}
