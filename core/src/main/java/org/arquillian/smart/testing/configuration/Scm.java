package org.arquillian.smart.testing.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.arquillian.smart.testing.scm.ScmRunnerProperties.DEFAULT_LAST_COMMITS;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;

public class Scm implements ConfigurationSection {

    private Range range;

    @SuppressWarnings("unused")
    public void setLastChanges(String lastChanges) {
        if (!Objects.equals(lastChanges, "0")) {
            final Range range = new Range();
            range.setHead(HEAD);
            range.setTail(String.join("~", HEAD, lastChanges));
            this.range = range;
        }
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        // To keep range set by scm.last.changes/lastChanges property.
        if (this.range == null) {
            this.range = range;
        }
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        final ArrayList<ConfigurationItem> configItems = new ArrayList<>();

        configItems.add(new ConfigurationItem("lastChanges", SCM_LAST_CHANGES, DEFAULT_LAST_COMMITS));

        return configItems;
    }
}
