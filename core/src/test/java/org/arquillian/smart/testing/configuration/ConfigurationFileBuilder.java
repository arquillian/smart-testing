package org.arquillian.smart.testing.configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

class ConfigurationFileBuilder {

    private HashMap<String, Object> properties;

    private ConfigurationFileBuilder() {
        properties = new HashMap<>();
    }

    static ConfigurationFileBuilder configurationFile() {
        return new ConfigurationFileBuilder();
    }

    ConfigurationFileBuilder inherit(String inherit) {
        properties.put("inherit", inherit);
        return this;
    }

    ConfigurationFileBuilder mode(String mode) {
        properties.put("mode", mode);
        return this;
    }

    ConfigurationFileBuilder applyTo(String applyTo) {
        properties.put("applyTo", applyTo);
        return this;
    }

    ConfigurationFileBuilder strategies(String strategies) {
        properties.put("strategies", strategies);
        return this;
    }

    ConfigurationFileBuilder debug(boolean debug) {
        properties.put("debug", debug);
        return this;
    }

    ConfigurationFileBuilder disable(boolean disable) {
        properties.put("disable", disable);
        return this;
    }

    ConfigurationFileBuilder scm() {
        properties.put("scm", new HashMap<>(0));
        return this;
    }

    ConfigurationFileBuilder range() {
        Map<String, Object> scm = new HashMap<>();
        scm.put("range", new HashMap<>(0));

        properties.put("scm", scm);
        return this;
    }

    ConfigurationFileBuilder head(String head) {
        final Map<String, Object> scm = (Map<String, Object>) properties.get("scm");
        final Map<String, String> range = (Map<String, String>) scm.get("range");
        range.put("head", head);

        return this;
    }

    ConfigurationFileBuilder tail(String tail) {
        final Map<String, Object> scm = (Map<String, Object>) properties.get("scm");
        final Map<String, String> range = (Map<String, String>) scm.get("range");
        range.put("tail", tail);

        return this;
    }

    void writeTo(Path filePath) {
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
