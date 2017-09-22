package org.arquillian.smart.testing.configuration;

import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;

public class Scm {

    private Range range;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }
}
