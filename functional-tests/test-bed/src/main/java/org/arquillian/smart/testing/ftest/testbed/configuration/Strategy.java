package org.arquillian.smart.testing.ftest.testbed.configuration;

public enum Strategy {
    AFFECTED,
    NEW;

    public String getName() {
        return this.name().toLowerCase();
    }

}
