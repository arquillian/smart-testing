package org.arquillian.smart.testing.configuration;

public class Report {

    private boolean enable;
    private String dir;
    private String name;

    public boolean isEnable() {
        return enable;
    }

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

    public static Builder builder() {
        return new Report.Builder();
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

        public Report build() {
            final Report report = new Report();
            report.enable = this.enable;
            report.dir = this.dir;
            report.name = this.name;

            return report;
        }
    }
}
