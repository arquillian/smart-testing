package org.arquillian.smart.testing.configuration;

import java.util.List;

public interface ConfigurationSection {

    List<ConfigurationItem> registerConfigurationItems();
}
