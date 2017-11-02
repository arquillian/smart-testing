package org.arquillian.smart.testing.vcs.git;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

import static org.arquillian.smart.testing.vcs.git.ChangedTestsDetector.CHANGED;

public class ChangedConfiguration implements StrategyConfiguration {

    @Override
    public String name() {
        return CHANGED;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        return new ArrayList<>();
    }
}
