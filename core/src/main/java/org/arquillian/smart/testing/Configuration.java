package org.arquillian.smart.testing;

public class Configuration {

    public static final String SMART_TESTING = "smart-testing";
    public static final String SMART_TESTING_MODE = "smart-testing-mode";

    private String[] strategies = new String[0];
    private RunMode mode;

    public static Configuration read() {
        final Configuration configuration = new Configuration();

        final String strategies = System.getProperty(SMART_TESTING, "").toLowerCase();

        if (containsAnyStrategy(strategies)) {
            configuration.strategies = strategies.split("\\s*,\\s*");
        }
        configuration.mode = RunMode.valueOf(System.getProperty(SMART_TESTING_MODE, "selecting").toUpperCase());

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

    public boolean areStrategies() {
        return strategies.length > 0;
    }

    public RunMode getMode() {
        return mode;
    }

    public String[] getStrategies() {
        return strategies;
    }
}
