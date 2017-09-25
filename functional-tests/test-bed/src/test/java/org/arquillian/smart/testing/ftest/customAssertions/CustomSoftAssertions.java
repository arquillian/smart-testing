package org.arquillian.smart.testing.ftest.customAssertions;

import org.assertj.core.api.SoftAssertions;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;

public class CustomSoftAssertions extends SoftAssertions {

    public BuiltProjectAssert assertThat(BuiltProject actual) {
        return proxy(BuiltProjectAssert.class, BuiltProject.class, actual);
    }
}
