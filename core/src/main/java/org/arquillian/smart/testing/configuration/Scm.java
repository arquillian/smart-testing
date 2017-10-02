package org.arquillian.smart.testing.configuration;

import java.util.ArrayList;
import java.util.List;

import static org.arquillian.smart.testing.scm.ScmRunnerProperties.DEFAULT_LAST_COMMITS;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;

public class Scm implements ConfigurationSection {

    private Range range;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    static Scm fromSystemProperties() {

        final String scmRangeHead = System.getProperty(SCM_RANGE_HEAD);
        final String scmRangeTail = System.getProperty(SCM_RANGE_TAIL);
        final String scmLastChanges = System.getProperty(SCM_LAST_CHANGES);

        if (scmLastChanges != null || (scmRangeHead != null && scmRangeTail != null)) {
            final Range range = new Range();
            if (scmRangeHead != null && scmRangeTail != null) {
                range.setHead(scmRangeHead);
                range.setTail(scmRangeTail);
            }

            if (scmLastChanges != null) {
                range.setHead(HEAD);
                range.setTail(String.join("~", HEAD, scmLastChanges));
            }

            final Scm scm = new Scm();
            scm.setRange(range);

            return scm;
        }

        return null;
    }

    static Scm fromDefaultValues() {
        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(String.join("~", HEAD, DEFAULT_LAST_COMMITS));

        final Scm scm = new Scm();
        scm.setRange(range);

        return scm;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        List<ConfigurationItem> configItems = new ArrayList<>();
        String tail = System.getProperty(SCM_LAST_CHANGES);
        if (tail == null){
            tail = DEFAULT_LAST_COMMITS;
        }
        String tailDefault = String.join("~", HEAD, tail);
        //configItems.add(new ConfigurationItem("head", SCM_RANGE_HEAD, HEAD));
        //configItems.add(new ConfigurationItem("tail", SCM_RANGE_TAIL, tailDefault));

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(tailDefault);
        configItems.add(new ConfigurationItem("range", null, range));
        return configItems;
    }
}
