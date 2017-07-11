package org.arquillian.smart.testing.ftest.testbed.configuration;

public enum Mode {
    SELECTING,
    ORDERING;


    public String getName() {
        return name().toLowerCase();
    }
}
