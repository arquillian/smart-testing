package org.arquillian.smart.testing.strategies.failed;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

public class FailedConfiguration implements StrategyConfiguration {

    static final String FAILED = "failed";

    @Override
    public String name() {
        return FAILED;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        return new ArrayList<>();
    }
}
