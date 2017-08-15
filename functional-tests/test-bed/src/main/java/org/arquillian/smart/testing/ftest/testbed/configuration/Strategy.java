package org.arquillian.smart.testing.ftest.testbed.configuration;

public enum Strategy {
    AFFECTED,
    NEW,
    CHANGED,
    FAILED;

    public String getName() {
        return this.name().toLowerCase();
    }

}
