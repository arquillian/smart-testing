package org.arquillian.smart.testing.report.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class Selection {

    private List<TestConfiguration> testConfigurations = new ArrayList<>();

    public Selection() {
    }

    Selection(Builder builder) {
        this.testConfigurations = builder.testConfigurations;
    }

    @XmlElementWrapper(name = "tests")
    @XmlElement(name = "test")
    public List<TestConfiguration> getTestConfigurations() {
        return testConfigurations;
    }


    public static class Builder {

        private List<TestConfiguration> testConfigurations = new ArrayList<>();
        private final SmartTestingExecution.Builder executionBuilder;

        Builder(SmartTestingExecution.Builder builder) {
            this.executionBuilder = builder;
        }

        public Builder withTestConfigurations(List<TestConfiguration> testConfigurations) {
            this.testConfigurations = testConfigurations;

            return this;
        }

        public Builder withTestConfigurations(TestConfiguration... testConfigurations) {
            this.testConfigurations = Arrays.asList(testConfigurations);

            return this;
        }

        public SmartTestingExecution.Builder done() {
            return this.executionBuilder.setSelection(this);
        }
    }

}
