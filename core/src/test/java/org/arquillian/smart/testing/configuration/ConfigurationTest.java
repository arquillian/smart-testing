package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.Configuration.loadConfigurationFromFile;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;

public class ConfigurationTest {

    @Test
    public void should_load_configuration_with_default_values_if_property_is_not_specified_in_config_file(){
        // given
        final ReportConfiguration reportConfiguration = ReportConfiguration.builder()
            .enable(true)
            .name("smart-testing-report.xml")
            .dir("target")
            .build();

        final ScmConfiguration scmConfiguration = ScmConfiguration.builder()
            .head(HEAD)
            .tail(HEAD + "~3")
            .build();

        final Configuration expectedConfiguration = Configuration.builder()
            .mode(ORDERING)
            .strategies(new String[] {"new", "changed", "affected"})
            .applyTo("surefire")
            .debug(true)
            .disable(false)
            .reportConfiguration(reportConfiguration)
            .scmConfiguration(scmConfiguration)
            .build();

        Map<String, Object> configMap = loadConfiguration();

        // when
        final Configuration actualConfiguration = Configuration.parseConfiguration(configMap);

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_dumped_configuration_from_file_as_configuration() {
        // given
        final ReportConfiguration reportConfiguration = ReportConfiguration.builder()
            .enable(false)
            .name("smart-testing-report.xml")
            .dir("target")
            .build();

        final ScmConfiguration scmConfiguration = ScmConfiguration.builder()
            .head(HEAD)
            .tail(HEAD + "~0")
            .build();

        final Configuration expectedConfiguration = Configuration.builder()
            .mode(SELECTING)
            .strategies(new String[] {"new"})
            .debug(false)
            .disable(false)
            .reportConfiguration(reportConfiguration)
            .scmConfiguration(scmConfiguration)
            .build();

        // when
        final Configuration actualConfiguration =
            loadConfigurationFromFile(new File("src/test/resources/dumped-smart-testing.yml"));

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    static Map<String, Object> loadConfiguration() {
        final Path path = Paths.get("src/test/resources/smart-testing.yml");
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(path)) {
            return (Map<String, Object>) yaml.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
