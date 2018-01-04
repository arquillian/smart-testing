package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import static java.util.Arrays.asList;
import static org.arquillian.smart.testing.Constants.CURRENT_DIR;
import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.loadConfigurationFromFile;
import static org.arquillian.smart.testing.configuration.ResourceLoader.getResourceAsFile;
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
        final Configuration expectedConfiguration = prepareConfigurationForConfigFile();

        // when
        final Configuration actualConfiguration =
            ConfigurationLoader.load(getResourceAsFile("configuration/smart-testing.yml"));

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_default_configuration() {
        // given
        final Configuration expectedConfiguration = prepareDefaultConfiguration();

        // when
        final Configuration defaultConfiguration = ConfigurationLoader.load(CURRENT_DIR);

        // then
        assertThat(defaultConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_default_configuration_and_log_warning_when_config_file_is_empty() throws IOException {
        // given
        final Configuration expectedConfiguration = prepareDefaultConfiguration();
        temporaryFolder.newFile(ConfigurationLoader.SMART_TESTING_YAML);

        // when
        final Configuration defaultConfiguration = ConfigurationLoader.load(temporaryFolder.getRoot());

        // then
        assertThat(defaultConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
        assertThat(systemErrOut.getLog())
            .contains("WARN: Smart Testing Extension - The configuration file "
                + temporaryFolder.getRoot().getPath() + File.separator + ConfigurationLoader.SMART_TESTING_YAML
                + " is empty");
    }

    @Test
    public void should_load_dumped_configuration_from_file_as_configuration() {
        // given
        final Configuration expectedConfiguration = prepareDefaultConfiguration();

        // when
        final Configuration actualConfiguration =
            loadConfigurationFromFile(getResourceAsFile("configuration/dumped-smart-testing.yml"));

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    static Configuration prepareDefaultConfiguration() {
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

    static Configuration prepareConfigurationForConfigFile() {
        final Report report = new Report();
        report.setEnable(true);
        report.setDir(TARGET);
        report.setName(REPORT_FILE_NAME);

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(HEAD + "~2");

        final Scm scm = new Scm();
        scm.setRange(range);

        Map<String, Object> affectedStrategiesConfig = new HashMap<>();
        affectedStrategiesConfig.put("exclusions", asList("org.package.*", "org.arquillian.package.*"));
        affectedStrategiesConfig.put("inclusions",
            asList("org.package.exclude.*", "org.arquillian.package.exclude.*"));
        affectedStrategiesConfig.put("transitivity", true);

        Map<String, Object> strategiesConfig = new HashMap<>();
        strategiesConfig.put("affected", affectedStrategiesConfig);

        final Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setMode(ORDERING);
        expectedConfiguration.setStrategies("new", "changed", "affected");
        expectedConfiguration.setApplyTo("surefire");
        expectedConfiguration.setDebug(true);
        expectedConfiguration.setDisable(false);
        expectedConfiguration.setScm(scm);
        expectedConfiguration.setReport(report);
        expectedConfiguration.setAutocorrect(true);
        expectedConfiguration.setStrategiesConfig(strategiesConfig);
        expectedConfiguration.setCustomStrategies(
            new String[] {"smart.testing.strategy.cool=org.arquillian.smart.testing:strategy-cool:1.0.0",
                "smart.testing.strategy.experimental=org.arquillian.smart.testing:strategy-experimental:1.0.0"});
        expectedConfiguration.setCustomProviders(
            new String[] {"org.foo:my-custom-provider=fully.qualified.name.to.SurefireProviderImpl"});

        return expectedConfiguration;
    }
}
