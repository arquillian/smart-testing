package org.arquillian.smart.testing.strategies.categorized.project.tags;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("included")
public class WithExcludedTagClass {

    @Test
    public void includedMethod() {

    }

    @Tag("excluded")
    @Test
    public void excludedMethod() {

    }
}
