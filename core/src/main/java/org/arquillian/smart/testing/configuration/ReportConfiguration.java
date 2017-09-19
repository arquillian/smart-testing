package org.arquillian.smart.testing.configuration;

public class ReportConfiguration {
    public boolean isEnable() {
        return enable;
    }

    private boolean enable;
    private String dir;
    private String name;

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ReportConfiguration.Builder builder() {
        return new ReportConfiguration.Builder();
    }

    public static class Builder {
        private boolean enable;
        private String dir;
        private String name;

        public Builder enable(boolean enable) {
            this.enable = enable;
            return this;
        }

        public Builder dir(String dir) {
            this.dir = dir;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ReportConfiguration build() {
            final ReportConfiguration reportConfiguration = new ReportConfiguration();
            reportConfiguration.enable = this.enable;
            reportConfiguration.dir = this.dir;
            reportConfiguration.name = this.name;
            return reportConfiguration;
        }
    }
}
