package org.arquillian.smart.testing.configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

class ConfigurationFile {

    private HashMap<String, Object> properties;

    private ConfigurationFile() {
        properties = new HashMap<>();
    }

    static ConfigurationFile SmartTestingConfigurationFile() {
        return new ConfigurationFile();
    }

    ConfigurationFile inherit(String inherit) {
        properties.put("inherit", inherit);
        return this;
    }

    ConfigurationFile mode(String mode) {
        properties.put("mode", mode);
        return this;
    }

    ConfigurationFile applyTo(String applyTo) {
        properties.put("applyTo", applyTo);
        return this;
    }

    ConfigurationFile strategies(String strategies) {
        properties.put("strategies", strategies);
        return this;
    }

    ConfigurationFile debug(boolean debug) {
        properties.put("debug", debug);
        return this;
    }

    ConfigurationFile disable(boolean disable) {
        properties.put("disable", disable);
        return this;
    }

    void create(Path filePath) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);
        try {
            FileWriter writer = new FileWriter(filePath.toString());
            yaml.dump(properties, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
