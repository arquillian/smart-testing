package org.arquillian.smart.testing.configuration;

import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;

public class Scm {

    private Range range;

    Scm(Builder builder) {
        this.range = builder.range;
    }

    Scm() {
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public static Builder builder() {
        return new Scm.Builder();
    }

    public static class Builder {
        private Range range;

        public Builder range(Range range) {
            this.range = range;
            return this;
        }

        public Builder lastChanges(String n) {
            this.range = Range.builder()
                            .head(HEAD)
                            .tail(String.join("~", HEAD, n))
                        .build();
            return this;
        }

        public Scm build() {
            return new Scm(this);
        }
    }
}
