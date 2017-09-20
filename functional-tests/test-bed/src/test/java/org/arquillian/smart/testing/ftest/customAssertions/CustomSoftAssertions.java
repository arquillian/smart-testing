package org.arquillian.smart.testing.ftest.customAssertions;

import org.assertj.core.api.SoftAssertions;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;

public class CustomSoftAssertions extends SoftAssertions {

    public BuildProjectAssert assertThat(BuiltProject actual) {
        return proxy(BuildProjectAssert.class, BuiltProject.class, actual);
    }
}
