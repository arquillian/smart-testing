package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.nio.file.Paths;
import org.junit.Test;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.Configuration.loadConfigurationFromFile;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.TARGET;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.DEFAULT_LAST_COMMITS;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTest {

    @Test
    public void should_load_configuration_with_default_values_if_property_is_not_specified_in_config_file() {
        // given
        final Report report = new Report();
        report.setEnable(true);
        report.setDir(TARGET);
        report.setName(REPORT_FILE_NAME);

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(HEAD + "~2");

        final Scm scm = new Scm();
        scm.setRange(range);

        final Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setMode(ORDERING);
        expectedConfiguration.setStrategies("new", "changed", "affected");
        expectedConfiguration.setApplyTo("surefire");
        expectedConfiguration.setDebug(true);
        expectedConfiguration.setDisable(false);
        expectedConfiguration.setScm(scm);
        expectedConfiguration.setReport(report);
        expectedConfiguration.setAutocorrect(true);
        expectedConfiguration.setCustomStrategies(
            new String[] {"smart.testing.strategy.cool=org.arquillian.smart.testing:strategy-cool:1.0.0",
                "smart.testing.strategy.experimental=org.arquillian.smart.testing:strategy-experimental:1.0.0"});

        // when
        final Configuration actualConfiguration =
            Configuration.load(Paths.get("src/test/resources/configuration/smart-testing.yml"));

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_default_configuration() {
        // given
        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(String.join("~", HEAD, DEFAULT_LAST_COMMITS));

        final Scm scm = new Scm();
        scm.setRange(range);

        final Report report = new Report();
        report.setEnable(false);
        report.setDir(TARGET);
        report.setName(REPORT_FILE_NAME);

        final Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setMode(SELECTING);
        expectedConfiguration.setDebug(false);
        expectedConfiguration.setDisable(false);
        expectedConfiguration.setReport(report);
        expectedConfiguration.setScm(scm);
        expectedConfiguration.setAutocorrect(false);

        // when
        final Configuration defaultConfiguration = Configuration.load();

        // then
        assertThat(defaultConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_dumped_configuration_from_file_as_configuration() {
        // given
        final Report report = new Report();
        report.setEnable(false);
        report.setDir(TARGET);
        report.setName(REPORT_FILE_NAME);

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(HEAD + "~0");

        final Scm scm = new Scm();
        scm.setRange(range);

        final Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setMode(SELECTING);
        expectedConfiguration.setStrategies("new");
        expectedConfiguration.setDebug(false);
        expectedConfiguration.setDisable(false);
        expectedConfiguration.setScm(scm);
        expectedConfiguration.setReport(report);

        // when
        final Configuration actualConfiguration =
            loadConfigurationFromFile(new File("src/test/resources/configuration/dumped-smart-testing.yml"));

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }
}
