package org.arquillian.smart.testing.configuration;

import java.util.ArrayList;
import java.util.List;

public class Range implements ConfigurationSection{

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
        configItems.add(new ConfigurationItem("head"));
        configItems.add(new ConfigurationItem("tail"));
        return configItems;
    }
}
