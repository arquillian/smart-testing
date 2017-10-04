package org.arquillian.smart.testing.ftest.testbed.configuration.builder;

import org.arquillian.smart.testing.configuration.Range;
import org.arquillian.smart.testing.configuration.Scm;

import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;

public class ScmBuilder {

    private Range range;

    private final ConfigurationBuilder configurationBuilder;

    ScmBuilder(ConfigurationBuilder builder) {
        this.configurationBuilder = builder;
    }

    public RangeBuilder range() {
        return new RangeBuilder(this);
    }

    public ScmBuilder lastChanges(String n) {
        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(String.join("~", HEAD, n));

        this.range = range;

        return this;
    }

    public ScmBuilder setRange(Range range) {
        this.range = range;

        return this;
    }

    public ConfigurationBuilder build() {
        final Scm scm = new Scm();
        scm.setRange(this.range);

        return this.configurationBuilder.setScm(scm);
    }
}
