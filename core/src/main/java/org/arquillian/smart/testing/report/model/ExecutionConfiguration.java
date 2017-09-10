package org.arquillian.smart.testing.report.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"usageMode", "strategies", "properties"})
public class ExecutionConfiguration {

    private List<String> strategies;
    private String usageMode;
    private List<Property> properties;

    public ExecutionConfiguration() {
    }

    ExecutionConfiguration(ExecutionConfiguration.Builder builder) {
        this.strategies = builder.strategies;
        this.usageMode = builder.usageMode;
        this.properties = builder.properties;
    }

    @XmlElement
    public String getUsageMode() {
        return usageMode;
    }

    @XmlElementWrapper(name = "strategies")
    @XmlElement(name = "strategy")
    public List<String> getStrategies() {
        return strategies;
    }

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public List<Property> getProperties() {
        return properties;
    }

    public static class Builder {
        private List<String> strategies;
        private String usageMode;
        private List<Property> properties;

        private final SmartTestingExecution.Builder executionBuilder;

        Builder(SmartTestingExecution.Builder builder) {
            this.executionBuilder = builder;
        }

        public Builder withUsageMode(String usageMode) {
            this.usageMode =usageMode;
            return this;
        }

        public Builder withStrategies(List<String> strategies) {
            this.strategies = strategies;
            return this;
        }

        public Builder withStrategies(String... strategies) {
            this.strategies = Arrays.asList(strategies);
            return this;
        }

        public SmartTestingExecution.Builder done() {
            return this.executionBuilder.setExecutionConfiguration(this);
        }

        public Builder withProperties(Map<String, String> properties) {

            this.properties = properties.entrySet().stream()
                .map(Property::new)
                .collect(Collectors.toList());
            return this;
        }
    }
}
