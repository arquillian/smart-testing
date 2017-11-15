package org.arquillian.smart.testing.configuration;

import java.util.ArrayList;
import java.util.List;

import static org.arquillian.smart.testing.scm.ScmRunnerProperties.DEFAULT_LAST_COMMITS;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;

public class Range implements ConfigurationSection {

    private String head;
    private String tail;

    public void setTail(String tail) {
        this.tail = tail;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getHead() {
        return head;
    }

    public String getTail() {
        return tail;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        List<ConfigurationItem> configItems = new ArrayList<>();

        String tail = String.join("~", HEAD, DEFAULT_LAST_COMMITS);

        final String scmLastChanges = System.getProperty(SCM_LAST_CHANGES);
        if (scmLastChanges != null) {
            tail = String.join("~", HEAD, scmLastChanges);
        }

        configItems.add(new ConfigurationItem("head", SCM_RANGE_HEAD, HEAD));
        configItems.add(new ConfigurationItem("tail", SCM_RANGE_TAIL, tail));

        return configItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Range range = (Range) o;

        if (head != null ? !head.equals(range.head) : range.head != null) return false;
        return tail != null ? tail.equals(range.tail) : range.tail == null;
    }

    @Override
    public int hashCode() {
        int result = head != null ? head.hashCode() : 0;
        result = 31 * result + (tail != null ? tail.hashCode() : 0);
        return result;
    }
}
