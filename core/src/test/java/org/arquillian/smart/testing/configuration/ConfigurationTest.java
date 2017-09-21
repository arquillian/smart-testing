package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
        final Report report = Report.builder()
                .enable(true)
                .name("smart-testing-report.xml")
                .dir("target")
            .build();

        final Scm scm = Scm.builder()
                .range(Range.builder().head(HEAD).tail(HEAD + "~3").build())
            .build();

        final Configuration expectedConfiguration = Configuration.builder()
                .mode(ORDERING)
                .strategies("new", "changed", "affected")
                .applyTo("surefire")
                .debug(true)
                .disable(false)
                .report(report)
                .scm(scm)
            .build();

        // when
        final Configuration actualConfiguration = Configuration.load(Paths.get("src/test/resources/smart-testing.yml"));

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_dumped_configuration_from_file_as_configuration() {
        // given
        final Report report = Report.builder()
                .enable(false)
                .name("smart-testing-report.xml")
                .dir("target")
            .build();

        final Scm scm = Scm.builder()
                .range(Range.builder().head(HEAD).tail(HEAD + "~0").build())
            .build();

        final Configuration expectedConfiguration = Configuration.builder()
                .mode(SELECTING)
                .strategies("new")
                .debug(false)
                .disable(false)
                .report(report)
                .scm(scm)
            .build();

        // when
        final Configuration actualConfiguration =
            loadConfigurationFromFile(new File("src/test/resources/dumped-smart-testing.yml"));

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }
}
