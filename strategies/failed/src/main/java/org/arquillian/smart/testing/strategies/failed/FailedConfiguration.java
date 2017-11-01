package org.arquillian.smart.testing.strategies.failed;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

import static org.arquillian.smart.testing.strategies.failed.FailedTestsDetector.FAILED;

public class FailedConfiguration implements StrategyConfiguration {

    @Override
    public String name() {
        return FAILED;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        return new ArrayList<>();
    }
}
