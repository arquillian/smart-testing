package org.arquillian.smart.testing.mvn.ext;

public class Configuration {

    private String[] strategies = new String[0];
    // TODO Change to smart testing mode
    private String mode;

    public static Configuration read() {
        Configuration configuration = new Configuration();
        configuration.strategies = System.getProperty("smart-testing", "")
            .split("\\s+,\\s+");
        configuration.mode = System.getProperty("smart-testing-mode", "selecting");

        return configuration;
    }

    public String getMode() {
        return mode;
    }

    public String[] getStrategies() {
        return strategies;
    }
}
