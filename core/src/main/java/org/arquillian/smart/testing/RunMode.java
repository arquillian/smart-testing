package org.arquillian.smart.testing;


public enum RunMode {

    SELECTING, ORDERING;

    public String getName() {
        return this.name().toLowerCase();
    }

}
