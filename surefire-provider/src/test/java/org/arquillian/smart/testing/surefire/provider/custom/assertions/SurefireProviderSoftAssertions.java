package org.arquillian.smart.testing.surefire.provider.custom.assertions;

import org.arquillian.smart.testing.surefire.provider.info.CustomProviderInfo;
import org.assertj.core.api.SoftAssertions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SurefireProviderSoftAssertions extends SoftAssertions implements TestRule {

    public CustomProviderAssert assertThat(CustomProviderInfo actual) {
        return proxy(CustomProviderAssert.class, CustomProviderInfo.class, actual);
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
