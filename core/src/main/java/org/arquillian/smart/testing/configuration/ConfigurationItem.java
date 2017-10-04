package org.arquillian.smart.testing.configuration;

class ConfigurationItem {

    private final String paramName;
    private String systemProperty;
    private Object defaultValue;

    /**
     * Constructor
     * @param paramName needs to match with name of setter method by excluding set/add.
     * @param systemProperty required to overwrite field value.
     * @param defaultValue to use for the field.
     */
    ConfigurationItem(String paramName, String systemProperty, Object defaultValue) {
        this.paramName = paramName;
        this.systemProperty = systemProperty;
        this.defaultValue = defaultValue;
    }

    ConfigurationItem(String paramName, String systemProperty) {
        this.paramName = paramName;
        this.systemProperty = systemProperty;
    }

    ConfigurationItem(String paramName) {
        this.paramName = paramName;
    }

    ConfigurationItem(String paramName, Object defaultValue) {
        this.paramName = paramName;
        this.defaultValue = defaultValue;
    }

    String getParamName() {
        return paramName;
    }

    String getSystemProperty() {
        return systemProperty;
    }

    Object getDefaultValue() {
        return defaultValue;
    }
}
