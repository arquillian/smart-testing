package org.arquillian.smart.testing.custom.assertions;

import org.arquillian.smart.testing.configuration.Configuration;
import org.assertj.core.api.SoftAssertions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.nio.file.Path;

public class CoreSoftAssertions extends SoftAssertions implements TestRule {

    public ConfigurationAssert assertThat(Configuration actual) {
        return proxy(ConfigurationAssert.class, Configuration.class, actual);
    }

    public DirectoryAssert assertThatDirectory(Path actual) {
        return proxy(DirectoryAssert.class, Path.class, actual);
    }

    public DirectoryAssert assertThatDirectory(File actual) {
        return assertThatDirectory(actual.toPath());
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            public void evaluate() throws Throwable {
                base.evaluate();
                assertAll();
            }
        };
    }
}
