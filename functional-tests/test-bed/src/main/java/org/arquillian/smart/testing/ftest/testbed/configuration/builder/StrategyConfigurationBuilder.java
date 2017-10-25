package org.arquillian.smart.testing.ftest.testbed.configuration.builder;

import java.util.Objects;
import org.arquillian.smart.testing.ftest.testbed.configuration.Strategy;
import org.arquillian.smart.testing.spi.StrategyConfiguration;
import org.arquillian.smart.testing.strategies.affected.AffectedConfiguration;

public class StrategyConfigurationBuilder {

    final StrategiesConfigurationBuilder strategiesConfigurationBuilder;
    private String name;
    private String config;
    private boolean transitivity = true;
    private String exclusions;
    private String inclusions;

    public StrategyConfigurationBuilder(StrategiesConfigurationBuilder strategiesConfigurationBuilder) {
        this.strategiesConfigurationBuilder = strategiesConfigurationBuilder;
    }

    public StrategyConfigurationBuilder name(Strategy strategy) {
        this.name = strategy.getName();
        return this;
    }

    public StrategyConfigurationBuilder name(String name) {
        this.name = name;
        return this;
    }

    public StrategyConfigurationBuilder config(String config) {
        this.config = config;
        return this;
    }

    public StrategyConfigurationBuilder transitivity(boolean transitivity) {
        this.transitivity = transitivity;
        return this;
    }

    public StrategyConfigurationBuilder exclusions(String exclusions) {
        this.exclusions = exclusions;
        return this;
    }

    public StrategyConfigurationBuilder inclusions(String inclusions) {
        this.inclusions = inclusions;
        return this;
    }

    public StrategiesConfigurationBuilder add() {
        if (Objects.equals(this.name, "affected")) {
            AffectedConfiguration strategyConfiguration = new AffectedConfiguration();
            strategyConfiguration.setTransitivity(this.transitivity);
            strategyConfiguration.setConfig(this.config);
            strategyConfiguration.setInclusions(this.inclusions);
            strategyConfiguration.setExclusions(this.exclusions);
            this.strategiesConfigurationBuilder.add(strategyConfiguration);
        }

        return this.strategiesConfigurationBuilder;
    }
}
