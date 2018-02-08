package org.arquillian.smart.testing.strategies.failed;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

import static org.arquillian.smart.testing.strategies.failed.FailedTestsDetector.FAILED;

public class FailedConfiguration implements StrategyConfiguration {


    private static final String SMART_TESTING_FAILED_METHODS = "smart.testing.failed.methods";
    private boolean methods;

    @Override
    public String name() {
        return FAILED;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        ArrayList<ConfigurationItem> items = new ArrayList<>();
        items.add(new ConfigurationItem("methods", SMART_TESTING_FAILED_METHODS, true));
        return items;
    }


    public boolean isMethods() {
        return methods;
    }

    public void setMethods(boolean methods) {
        this.methods = methods;
    }
}
