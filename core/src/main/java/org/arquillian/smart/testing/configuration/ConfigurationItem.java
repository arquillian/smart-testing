package org.arquillian.smart.testing.configuration;

public class ConfigurationItem {

    private final String paramName;
    private String systemProperty;
    private Object defaultValue;

    /**
     * @param paramName needs to match with name of setter method by excluding set/add.
     * @param systemProperty required to overwrite field value.
     * @param defaultValue to use for the field.
     */
    public ConfigurationItem(String paramName, String systemProperty, Object defaultValue) {
        this.paramName = paramName;
        this.systemProperty = systemProperty;
        this.defaultValue = defaultValue;
    }

    /**
     * @param paramName needs to match with name of setter method by excluding set/add.
     * @param systemProperty required to overwrite field value.
     */
    public ConfigurationItem(String paramName, String systemProperty) {
        this.paramName = paramName;
        this.systemProperty = systemProperty;
    }

    /**
     * @param paramName needs to match with name of setter method by excluding set/add.
     */
    public ConfigurationItem(String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }

    public String getSystemProperty() {
        return systemProperty;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
