package org.arquillian.smart.testing.custom.assertions;

import org.arquillian.smart.testing.RunMode;
import org.arquillian.smart.testing.configuration.Configuration;
import org.assertj.core.api.AbstractAssert;

import java.util.Arrays;
import java.util.Objects;

public class ConfigurationAssert extends AbstractAssert<ConfigurationAssert, Configuration> {

    ConfigurationAssert(Configuration actual) {
        super(actual, ConfigurationAssert.class);
    }

    public static ConfigurationAssert assertThat(Configuration config) {
        return new ConfigurationAssert(config);
    }

    public ConfigurationAssert hasMode(RunMode mode) {
        isNotNull();

        if (!Objects.equals(actual.getMode(), mode)) {
            failWithMessage("Expected run mode to be <%s> but was <%s>", mode, actual.getMode());
        }
        return this;
    }

    public ConfigurationAssert isAppliedTo(String applyTo) {
        isNotNull();

        if (!Objects.equals(actual.getApplyTo(), applyTo)) {
            failWithMessage("Expected plugin to be <%s> but was <%s>", applyTo, actual.getApplyTo());
        }
        return this;
    }

    public ConfigurationAssert hasAppliedStrategies(String[] strategies) {
        isNotNull();

        if (!Arrays.equals(actual.getStrategies(), strategies)) {
            failWithMessage("Expected applied strategies to be <%s> but was <%s>",
                Arrays.toString(strategies), Arrays.toString(actual.getStrategies()));
        }
        return this;
    }

    public ConfigurationAssert hasDebugEnable(boolean debug) {
        isNotNull();

        if (!Objects.equals(actual.isDebug(), debug)) {
            failWithMessage("Expected debug to be <%s> but was <%s>", debug, actual.isDebug());
        }
        return this;
    }
}
