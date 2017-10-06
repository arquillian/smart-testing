package org.arquillian.smart.testing.ftest.customAssertions;

import org.assertj.core.api.SoftAssertions;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

public class SmartTestingSoftAssertions extends SoftAssertions implements TestRule {

    public BuiltProjectAssert assertThat(BuiltProject actual) {
        return proxy(BuiltProjectAssert.class, BuiltProject.class, actual);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            public void evaluate() throws Throwable {
                base.evaluate();
                MultipleFailureException.assertEmpty(SmartTestingSoftAssertions.this.errorsCollected());
            }
        };
    }
}
