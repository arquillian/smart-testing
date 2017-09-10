package org.arquillian.smart.testing.report.model;

import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;

public class Property {

    private String key;
    private String value;

    public Property() {
    }

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Property(Map.Entry<String, String> entry) {
        this(entry.getKey(), entry.getValue());
    }

    @XmlAttribute(name = "name")
    public String getKey() {
        return key;
    }

    @XmlAttribute
    public String getValue() {
        return value;
    }

}
