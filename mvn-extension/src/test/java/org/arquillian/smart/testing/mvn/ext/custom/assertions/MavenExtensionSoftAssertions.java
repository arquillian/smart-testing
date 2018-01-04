package org.arquillian.smart.testing.mvn.ext.custom.assertions;

import org.arquillian.smart.testing.mvn.ext.checker.SkipInstallationChecker;
import org.assertj.core.api.SoftAssertions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MavenExtensionSoftAssertions extends SoftAssertions implements TestRule {

    public SkipInstallationCheckerAssert assertThat(SkipInstallationChecker actual) {
        return proxy(SkipInstallationCheckerAssert.class, SkipInstallationChecker.class, actual);
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
