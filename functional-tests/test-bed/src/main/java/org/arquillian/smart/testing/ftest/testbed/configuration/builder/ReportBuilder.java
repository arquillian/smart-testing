package org.arquillian.smart.testing.ftest.testbed.configuration.builder;

import org.arquillian.smart.testing.configuration.Report;

public class ReportBuilder {

    private boolean enable;
    private String dir;
    private String name;

    private final ConfigurationBuilder configurationBuilder;

    ReportBuilder(ConfigurationBuilder builder) {
        this.configurationBuilder = builder;
    }

    public ReportBuilder enable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public ReportBuilder dir(String dir) {
        this.dir = dir;
        return this;
    }

    public ReportBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ConfigurationBuilder build() {
        final Report report = new Report();
        report.setEnable(this.enable);
        report.setDir(this.dir);
        report.setName(this.name);

        return this.configurationBuilder.setReport(report);
    }
}
