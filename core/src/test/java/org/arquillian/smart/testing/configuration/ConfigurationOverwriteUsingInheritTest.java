package org.arquillian.smart.testing.configuration;

import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.ConfigurationFileBuilder.configurationFile;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationOverwriteUsingInheritTest {

    private static final String IMPL_BASE = "impl-base";
    static final String CONFIG = "config";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_load_configuration_properties_from_absolute_inherit_path_if_not_defined_in_child() throws IOException {
        // given
        final String root = temporaryFolder.getRoot().toString();

        configurationFile()
            .mode("ordering")
            .applyTo("surefire")
            .inherit(Paths.get(root, SMART_TESTING_YAML).toString())
            .writeTo(Paths.get(root, SMART_TESTING_YML));

        configurationFile()
            .strategies("new, changed, affected")
            .writeTo(Paths.get(root, SMART_TESTING_YAML));

        // when
        final Configuration configuration = ConfigurationLoader.load(temporaryFolder.getRoot());

        // then
        assertThat(configuration.getMode()).isEqualTo(ORDERING);
        assertThat(configuration.getApplyTo()).isEqualTo("surefire");
        assertThat(configuration.getStrategies()).isEqualTo(new String[]{"new", "changed", "affected"});
    }

    @Test
    public void should_load_configuration_properties_from_relative_inherit_path_if_not_defined_in_child () throws IOException {
        // given
        temporaryFolder.newFolder(CONFIG, IMPL_BASE);
        final String root = temporaryFolder.getRoot().toString();

        configurationFile()
            .inherit("../smart-testing.yml")
            .mode("selecting")
            .debug(true)
            .writeTo(Paths.get(root, CONFIG, IMPL_BASE, SMART_TESTING_YML));

        configurationFile()
            .inherit("../smart-testing.yml")
            .writeTo(Paths.get(root, CONFIG, SMART_TESTING_YML));

        configurationFile()
            .strategies("new, changed, affected")
            .writeTo(Paths.get(root, SMART_TESTING_YML));

        // when
        final Configuration configuration = ConfigurationLoader.load(Paths.get(root, CONFIG, IMPL_BASE).toFile());

        // then
        assertThat(configuration.getMode()).isEqualTo(SELECTING);
        assertThat(configuration.isDebug()).isTrue();
        assertThat(configuration.getStrategies()).isEqualTo(new String[]{"new", "changed", "affected"});
    }

    @Test
    public void should_not_overwrite_disable_parameter_from_inherit_path() throws IOException {
        // given
        temporaryFolder.newFolder(CONFIG);
        final String root = temporaryFolder.getRoot().toString();

        configurationFile()
            .inherit("../smart-testing.yml")
            .mode("ordering")
            .disable(true)
            .writeTo(Paths.get(root, CONFIG, SMART_TESTING_YML));

        configurationFile()
            .strategies("new, changed, affected")
            .disable(false)
            .writeTo(Paths.get(root, SMART_TESTING_YML));

        // when
        final Configuration configuration = ConfigurationLoader.load(Paths.get(root, CONFIG).toFile());

        // then
        assertThat(configuration.getMode()).isEqualTo(ORDERING);
        assertThat(configuration.isDisable()).isTrue();
        assertThat(configuration.getStrategies()).isEqualTo(new String[]{"new", "changed", "affected"});
    }

    @Test
    public void should_aggregate_nested_parameters_from_inherit_path() throws IOException {
        // given
        temporaryFolder.newFolder(CONFIG);
        final String root = temporaryFolder.getRoot().toString();

        configurationFile()
            .inherit("../smart-testing.yml")
            .mode("ordering")
            .disable(true)
            .scm()
                .range()
                .head(HEAD)
            .writeTo(Paths.get(root, CONFIG, SMART_TESTING_YML));

        configurationFile()
            .strategies("new, changed, affected")
            .disable(false)
            .scm()
                .range()
                .tail(HEAD + "~1")
            .writeTo(Paths.get(root, SMART_TESTING_YML));

        // when
        final Configuration configuration = ConfigurationLoader.load(Paths.get(root, CONFIG).toFile());

        // then
        final Range range = configuration.getScm().getRange();
        assertThat(configuration.getMode()).isEqualTo(ORDERING);
        assertThat(configuration.isDisable()).isTrue();
        assertThat(configuration.getStrategies()).isEqualTo(new String[]{"new", "changed", "affected"});
        assertThat(range.getHead()).isEqualTo(HEAD);
        assertThat(range.getTail()).isEqualTo(HEAD + "~1");
    }
}
