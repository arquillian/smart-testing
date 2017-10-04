package org.arquillian.smart.testing.ftest.testbed.configuration.builder;

import org.arquillian.smart.testing.configuration.Report;

public class ReportBuilder {

    private boolean enable;

    private final ConfigurationBuilder configurationBuilder;

    ReportBuilder(ConfigurationBuilder builder) {
        this.configurationBuilder = builder;
    }

    public ReportBuilder enable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public ConfigurationBuilder build() {
        final Report report = new Report();
        report.setEnable(this.enable);

        return this.configurationBuilder.setReport(report);
    }
}
