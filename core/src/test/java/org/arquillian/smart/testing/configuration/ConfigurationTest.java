package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.loadConfigurationFromFile;
import static org.arquillian.smart.testing.configuration.ResourceLoader.getResourceAsFile;
import static org.arquillian.smart.testing.configuration.ResourceLoader.getResourceAsPath;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.TARGET;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.DEFAULT_LAST_COMMITS;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ConfigurationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final SystemErrRule systemErrOut = new SystemErrRule().enableLog();

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
            ConfigurationLoader.load(getResourceAsPath("configuration/smart-testing.yml"));

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_default_configuration() {
        // given
        Configuration expectedConfiguration = prepareDefaultConfiguration();

        // when
        final Configuration defaultConfiguration = ConfigurationLoader.load();

        // then
        assertThat(defaultConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_default_configuration_and_log_warning_when_config_file_is_empty() throws IOException {
        // given
        Configuration expectedConfiguration = prepareDefaultConfiguration();
        temporaryFolder.newFile(ConfigurationLoader.SMART_TESTING_YAML);

        // when
        final Configuration defaultConfiguration = ConfigurationLoader.load(temporaryFolder.getRoot());

        // then
        assertThat(defaultConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
        assertThat(systemErrOut.getLog())
            .contains("WARN: Smart Testing Extension - The configuration file " + temporaryFolder.getRoot().getPath() + File.separator + "smart-testing.yaml is empty");
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
            loadConfigurationFromFile(getResourceAsFile("configuration/dumped-smart-testing.yml"));

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    private Configuration prepareDefaultConfiguration() {
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

        return expectedConfiguration;
    }
}
