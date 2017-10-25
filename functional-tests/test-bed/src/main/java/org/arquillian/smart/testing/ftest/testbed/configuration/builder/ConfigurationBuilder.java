package org.arquillian.smart.testing.ftest.testbed.configuration.builder;

import java.util.Arrays;
import java.util.List;
import org.arquillian.smart.testing.RunMode;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.Report;
import org.arquillian.smart.testing.configuration.Scm;
import org.arquillian.smart.testing.ftest.testbed.configuration.Strategy;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

public class ConfigurationBuilder {
    private String[] strategies;
    private RunMode mode;
    private String applyTo;
    private boolean disable;
    private boolean debug;
    private Report report;
    private Scm scm;
    private List<StrategyConfiguration> strategiesConfiguration;

    public ConfigurationBuilder strategies(String... strategies) {
        this.strategies = strategies;
        return this;
    }

    public ConfigurationBuilder strategies(Strategy... strategies) {
        this.strategies = Arrays.stream(strategies).map(Strategy::getName).toArray(String[]::new);
        return this;
    }

    public ConfigurationBuilder mode(RunMode mode) {
        this.mode = mode;
        return this;
    }

    public ConfigurationBuilder mode(String mode) {
        mode(RunMode.valueOf(mode.toUpperCase()));
        return this;
    }

    public ConfigurationBuilder applyTo(String applyTo) {
        this.applyTo = applyTo;
        return this;
    }

    public ConfigurationBuilder disable(boolean disable) {
        this.disable = disable;
        return this;
    }

    public ConfigurationBuilder debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public ReportBuilder report() {
        return new ReportBuilder(this);
    }

    public ScmBuilder scm() {
        return new ScmBuilder(this);
    }

    public StrategiesConfigurationBuilder strategiesConfiguration() {
        return new StrategiesConfigurationBuilder(this);
    }

    public ConfigurationBuilder setScm(Scm scm) {
        this.scm = scm;
        return this;
    }

    public ConfigurationBuilder setReport(Report report) {
        this.report = report;
        return this;
    }

    ConfigurationBuilder setStrategiesConfiguration(List<StrategyConfiguration> strategiesConfiguration) {
        this.strategiesConfiguration = strategiesConfiguration;

        return this;
    }

    public Configuration build() {

        final Configuration configuration = new Configuration();
        configuration.setStrategies(this.strategies);
        configuration.setMode(this.mode);
        configuration.setApplyTo(this.applyTo);
        configuration.setDisable(this.disable);
        configuration.setDebug(this.debug);
        configuration.setReport(this.report);
        configuration.setScm(this.scm);
        configuration.setStrategiesConfiguration(this.strategiesConfiguration);

        return configuration;
    }
}
