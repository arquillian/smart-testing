package org.arquillian.smart.testing.configuration;

import java.util.ArrayList;
import java.util.List;

import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_REPORT_ENABLE;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.TARGET;

public class Report implements ConfigurationSection {

    private Boolean enable;
    private String name = REPORT_FILE_NAME;
    private String dir = TARGET;

    public Boolean isEnable() {
        return enable != null && enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    static Report fromSystemProperties() {
        final String reportEnable = System.getProperty(SMART_TESTING_REPORT_ENABLE);
        if (reportEnable == null) {
            return null;
        }

        final Report report = new Report();
        report.setEnable(Boolean.valueOf(reportEnable));

        return report;
    }

    static Report fromDefaultValues() {
        final Report report = new Report();
        report.setEnable(false);

        return report;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        List<ConfigurationItem> configItems = new ArrayList<>();
        configItems.add(new ConfigurationItem("enable", SMART_TESTING_REPORT_ENABLE, false));
        configItems.add(new ConfigurationItem("name", null, REPORT_FILE_NAME));
        configItems.add(new ConfigurationItem("dir", null, TARGET));
        return configItems;
    }
}
