package org.arquillian.smart.testing.ftest.testbed.configuration.builder;

import java.util.List;
import java.util.Objects;
import org.arquillian.smart.testing.ftest.testbed.configuration.Strategy;
import org.arquillian.smart.testing.spi.StrategyConfiguration;
import org.arquillian.smart.testing.strategies.affected.AffectedConfiguration;

public class StrategyConfigurationBuilder {

    final StrategiesConfigurationBuilder strategiesConfigurationBuilder;
    private String name;
    private boolean transitivity = true;
    private List<String> exclusions;
    private List<String> inclusions;

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

    public StrategyConfigurationBuilder transitivity(boolean transitivity) {
        this.transitivity = transitivity;
        return this;
    }

    public StrategyConfigurationBuilder exclusions(List<String> exclusions) {
        this.exclusions = exclusions;
        return this;
    }

    public StrategyConfigurationBuilder inclusions(List<String> inclusions) {
        this.inclusions = inclusions;
        return this;
    }

    public StrategiesConfigurationBuilder add() {
        if (Objects.equals(this.name, "affected")) {
            AffectedConfiguration strategyConfiguration = new AffectedConfiguration();
            strategyConfiguration.setTransitivity(this.transitivity);
            strategyConfiguration.setInclusions(this.inclusions);
            strategyConfiguration.setExclusions(this.exclusions);
            this.strategiesConfigurationBuilder.add(strategyConfiguration);
        }

        return this.strategiesConfigurationBuilder;
    }
}
