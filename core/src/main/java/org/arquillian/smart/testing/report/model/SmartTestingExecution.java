package org.arquillian.smart.testing.report.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "smart-testing-execution")
@XmlType(propOrder = {"module", "executionConfiguration", "selection"})
public class SmartTestingExecution {

    private String module;
    private ExecutionConfiguration executionConfiguration;
    private Selection selection;

    public SmartTestingExecution() {
    }

    private SmartTestingExecution(SmartTestingExecution.Builder builder) {
        this.module = builder.module;
        this.executionConfiguration = builder.executionConfiguration;
        this.selection = builder.selection;
    }

    @XmlElement
    public Selection getSelection() {
        return selection;
    }

    @XmlElement
    public ExecutionConfiguration getExecutionConfiguration() {
        return executionConfiguration;
    }

    @XmlElement
    public String getModule() {
        return module;
    }

    public static SmartTestingExecution.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String module;
        private ExecutionConfiguration executionConfiguration;
        private Selection selection;

        public Builder() {
        }

        public Builder withModule(String name) {
            this.module = name;
            return this;
        }

        public ExecutionConfiguration.Builder addConfiguration() {
            return new ExecutionConfiguration.Builder(this);
        }

        public Builder setExecutionConfiguration(ExecutionConfiguration.Builder builder) {
            executionConfiguration = new ExecutionConfiguration(builder);
            return this;
        }

        public Builder setSelection(Selection.Builder builder) {
            this.selection = new Selection(builder);
            return this;
        }

        public Selection.Builder addSelection() {
            return new Selection.Builder(this);
        }

        public SmartTestingExecution build() {
            return new SmartTestingExecution(this);
        }
    }
}
