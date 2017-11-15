package org.arquillian.smart.testing.configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationOverWriteUsingInheretTest {

    private static final String IMPL_BASE = "impl-base";
    static final String CONFIG = "config";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_load_configuration_properties_from_absolute_inherit_if_not_defined_in_child() throws IOException {
        // given
        final String root = temporaryFolder.getRoot().toString();
        Map<String, Object> child = new HashMap<>();
        child.put("mode", "ordering");
        child.put("applyTo", "surefire");
        child.put("inherit", Paths.get(root, SMART_TESTING_YAML).toString());

        dumpData(Paths.get(root, SMART_TESTING_YML), child);

        Map<String, Object> parent = new HashMap<>();
        parent.put("strategies", "new, changed, affected");
        dumpData(Paths.get(root, SMART_TESTING_YAML), parent);

        // when
        final Configuration configuration = ConfigurationLoader.load(temporaryFolder.getRoot());

        // then
        assertThat(configuration.getMode()).isEqualTo(ORDERING);
        assertThat(configuration.getApplyTo()).isEqualTo("surefire");
        assertThat(configuration.getStrategies()).isEqualTo(new String[]{"new", "changed", "affected"});
    }

    @Test
    public void should_load_configuration_properties_from_relative_inherit_if_not_defined_in_child () throws IOException {
        // given
        temporaryFolder.newFolder(CONFIG, IMPL_BASE);
        final String root = temporaryFolder.getRoot().toString();
        Map<String, Object> child = new HashMap<>();
        child.put("inherit", "../smart-testing.yml");

        dumpData(Paths.get(root, CONFIG, SMART_TESTING_YML), child);

        Map<String, Object> child2 = new HashMap<>();
        child2.put("mode", "selecting");
        child2.put("debug", "true");
        child2.put("inherit", "../smart-testing.yml");

        dumpData(Paths.get(root, CONFIG, IMPL_BASE, SMART_TESTING_YML), child2);

        Map<String, Object> parent = new HashMap<>();
        parent.put("strategies", "new, changed, affected");
        dumpData(Paths.get(root, SMART_TESTING_YML), parent);

        // when
        final Configuration configuration = ConfigurationLoader.load(Paths.get(root, CONFIG, IMPL_BASE));

        // then
        assertThat(configuration.getMode()).isEqualTo(SELECTING);
        assertThat(configuration.isDebug()).isTrue();
        assertThat(configuration.getStrategies()).isEqualTo(new String[]{"new", "changed", "affected"});
    }

    @Test
    public void should_not_overwrite_disable_parameter_from_inherit() throws IOException {
        // given
        temporaryFolder.newFolder(CONFIG);
        final String root = temporaryFolder.getRoot().toString();
        Map<String, Object> child = new HashMap<>();
        child.put("mode", "ordering");
        child.put("disable", true);
        child.put("inherit", "../smart-testing.yml");

        dumpData(Paths.get(root, CONFIG, SMART_TESTING_YML), child);

        Map<String, Object> parent = new HashMap<>();
        parent.put("strategies", "new, changed, affected");
        parent.put("disable", false);
        dumpData(Paths.get(root, SMART_TESTING_YML), parent);

        // when
        final Configuration configuration = ConfigurationLoader.load(Paths.get(root, CONFIG));

        // then
        assertThat(configuration.getMode()).isEqualTo(ORDERING);
        assertThat(configuration.isDisable()).isTrue();
        assertThat(configuration.getStrategies()).isEqualTo(new String[]{"new", "changed", "affected"});
    }

    static void dumpData(Path filePath, Map<String, Object> data) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);
        FileWriter writer = new FileWriter(filePath.toString());
        yaml.dump(data, writer);
    }
}
