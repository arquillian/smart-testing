package org.arquillian.smart.testing.report.model;

import java.util.Collection;
import javax.xml.bind.annotation.XmlAttribute;
import org.eclipse.jgit.util.StringUtils;

public class TestConfiguration {

    private String strategies;
    private String name;

    public TestConfiguration() {
    }

    private TestConfiguration(TestConfiguration.Builder builder) {
        this.strategies = builder.strategies;
        this.name = builder.name;
    }

    @XmlAttribute
    public String getStrategies() {
        return strategies;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    public static TestConfiguration.Builder builder() {
        return new TestConfiguration.Builder();
    }

    public static class Builder {

        private String strategies;
        private String name;

        private TestConfiguration.Builder testBuilder;

        public TestConfiguration.Builder withName(String className) {
            this.name = className;
            return this;
        }


        public TestConfiguration.Builder withStrategies(Collection<String> strategies) {
            this.strategies = StringUtils.join(strategies, ",");
            return this;
        }

        public TestConfiguration build() {
            return new TestConfiguration(this);
        }
    }
}
