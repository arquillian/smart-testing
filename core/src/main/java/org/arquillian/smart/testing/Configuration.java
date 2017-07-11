package org.arquillian.smart.testing;

public class Configuration {

    public static final String SMART_TESTING = "smart-testing";
    public static final String SMART_TESTING_MODE = "smart-testing-mode";

    private String[] strategies = new String[0];
    // TODO Change to smart testing mode
    private RunMode mode;

    public static Configuration read() {
        Configuration configuration = new Configuration();
        configuration.strategies = System.getProperty(SMART_TESTING, "").toLowerCase()
            .split("\\s*,\\s*");
        configuration.mode = RunMode.valueOf(System.getProperty(SMART_TESTING_MODE, "ordering").toUpperCase());

        return configuration;
    }

    public boolean isSelectingMode() {
        return isModeSet() && RunMode.SELECTING == getMode();
    }

    public boolean isModeSet() {
        return this.mode != null;
    }

    public RunMode getMode() {
        return mode;
    }

    public String[] getStrategies() {
        return strategies;
    }
}
