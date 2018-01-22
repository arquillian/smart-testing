package org.arquillian.smart.testing.strategies.categorized.project.tags;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class WithTaggedMethodsClass {

    @Test
    @Tag("fast")
    public void testWithFastTag() {
    }

    @Test
    @Tag("first")
    public void testWithFirstTag() {
    }

    @Test
    @Tag("second")
    public void testWithSecondTag() {
    }

    @Test
    @Tag("third")
    public void testWithThirdTag() {
    }

    @Test
    public void testWithoutTag() {
    }
}
