package org.arquillian.smart.testing.configuration;

import java.util.ArrayList;
import java.util.List;

import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_REPORT_ENABLE;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.TARGET;

public class Report implements ConfigurationSection {

    private boolean enable;
    private String name;
    private String dir;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
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

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        List<ConfigurationItem> configItems = new ArrayList<>();
        configItems.add(new ConfigurationItem("enable", SMART_TESTING_REPORT_ENABLE, false));
        configItems.add(new ConfigurationItem("name", null, REPORT_FILE_NAME));
        configItems.add(new ConfigurationItem("dir", null, TARGET));
        return configItems;
    }
}
