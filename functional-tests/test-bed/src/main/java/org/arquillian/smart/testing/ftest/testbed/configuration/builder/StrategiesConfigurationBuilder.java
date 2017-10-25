package org.arquillian.smart.testing.ftest.testbed.configuration.builder;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

public class StrategiesConfigurationBuilder {

    private final ConfigurationBuilder configurationBuilder;
    private List<StrategyConfiguration> strategiesConfiguration = new ArrayList<>();

    public StrategiesConfigurationBuilder(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }

    public StrategyConfigurationBuilder strategyConfiguration() {
        return new StrategyConfigurationBuilder(this);
    }

    public StrategiesConfigurationBuilder add(StrategyConfiguration strategyConfiguration) {
        this.strategiesConfiguration.add(strategyConfiguration);

        return this;
    }

    public ConfigurationBuilder build() {
        return configurationBuilder.setStrategiesConfiguration(this.strategiesConfiguration);
    }
}
