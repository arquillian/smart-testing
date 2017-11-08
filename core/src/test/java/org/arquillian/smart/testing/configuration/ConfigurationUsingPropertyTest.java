package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_MODE;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_REPORT_ENABLE;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_CONFIG;
import static org.arquillian.smart.testing.configuration.ResourceLoader.getResourceAsFile;
import static org.arquillian.smart.testing.configuration.ResourceLoader.getResourceAsPath;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.TARGET;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.DEFAULT_LAST_COMMITS;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;
import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ConfigurationUsingPropertyTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final SystemErrRule systemErrOut = new SystemErrRule().enableLog();

    @Test
    public void should_load_configuration_with_overwriting_system_property_for_scmLastChanges_over_values_from_config_file() {
        // given
        System.setProperty(SCM_LAST_CHANGES, "3");

        // when
        final Configuration actualConfiguration =
            ConfigurationLoader.load(getResourceAsPath("configuration/smart-testing-with-lastChanges.yml"));

        // then
        final Range range = actualConfiguration.getScm().getRange();
        assertThat(range).extracting("head", "tail").containsExactly(HEAD, HEAD + "~3");
    }

    @Test
    public void should_load_configuration_for_scmLastChanges_from_config_file() {
        // when
        final Configuration actualConfiguration =
            ConfigurationLoader.load(getResourceAsPath("configuration/smart-testing-with-lastChanges.yml"));

        // then
        final Range range = actualConfiguration.getScm().getRange();
        assertThat(range).extracting("head", "tail").containsExactly(HEAD, HEAD + "~1");
    }

    @Test
    public void should_load_configuration_for_rangeHead_and_rangeTail_from_config_file() {
        // when
        final Configuration actualConfiguration =
            ConfigurationLoader.load(getResourceAsPath("configuration/smart-testing.yml"));

        // then
        final Range range = actualConfiguration.getScm().getRange();
        assertThat(range).extracting("head", "tail").containsExactly(HEAD, HEAD + "~2");
    }

    @Test
    public void should_load_configuration_with_overwriting_system_property_for_rangeHead_and_rangeTail_over_values_from_config_file() {
        // given
        System.setProperty(SCM_RANGE_HEAD, HEAD + "~1");
        System.setProperty(SCM_RANGE_TAIL, HEAD + "~5");

        // when
        final Configuration actualConfiguration =
            ConfigurationLoader.load(getResourceAsPath("configuration/smart-testing.yml"));

        // then
        final Range range = actualConfiguration.getScm().getRange();
        assertThat(range).extracting("head", "tail").containsExactly(HEAD + "~1", HEAD + "~5");
    }

    @Test
    public void should_load_configuration_with_overwriting_system_properties_over_properties_from_config_file() {
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SMART_TESTING_MODE, "selecting");
        System.setProperty(SCM_RANGE_TAIL, HEAD + "~4");
        System.setProperty("smart.testing.strategy.my", "org.arquillian.smart.testing:strategy-my:1.0.0");
        System.setProperty("smart.testing.strategy.cool", "org.arquillian.smart.testing:strategy-cool:1.0.1");

        final Report report = new Report();
        report.setEnable(true);
        report.setDir(TARGET);
        report.setName(REPORT_FILE_NAME);

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(HEAD + "~4");

        final Scm scm = new Scm();
        scm.setRange(range);

        Map<String, Object> affectedStrategiesConfig = new HashMap<>();
        affectedStrategiesConfig.put("exclusions", Arrays.asList("org.package.*", "org.arquillian.package.*"));
        affectedStrategiesConfig.put("inclusions",
            Arrays.asList("org.package.exclude.*", "org.arquillian.package.exclude.*"));
        affectedStrategiesConfig.put("transitivity", true);

        Map<String, Object> strategiesConfig = new HashMap<>();
        strategiesConfig.put("affected", affectedStrategiesConfig);

        final Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setMode(SELECTING);
        expectedConfiguration.setStrategies("changed");
        expectedConfiguration.setApplyTo("surefire");
        expectedConfiguration.setDebug(true);
        expectedConfiguration.setDisable(false);
        expectedConfiguration.setReport(report);
        expectedConfiguration.setScm(scm);
        expectedConfiguration.setAutocorrect(true);
        expectedConfiguration.setStrategiesConfig(strategiesConfig);
        expectedConfiguration.setCustomStrategies(
            new String[] {"smart.testing.strategy.experimental=org.arquillian.smart.testing:strategy-experimental:1.0.0",
                "smart.testing.strategy.my=org.arquillian.smart.testing:strategy-my:1.0.0",
                "smart.testing.strategy.cool=org.arquillian.smart.testing:strategy-cool:1.0.1"});

        // when
        final Configuration actualConfiguration =
            ConfigurationLoader.load(getResourceAsPath("configuration/smart-testing.yml"));

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_configuration_with_defaults_and_with_specified_system_properties_if_config_file_is_not_given() {
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SMART_TESTING_REPORT_ENABLE, "true");

        final Report report = new Report();
        report.setEnable(true);
        report.setDir(TARGET);
        report.setName(REPORT_FILE_NAME);

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(HEAD + "~0");

        final Scm scm = new Scm();
        scm.setRange(range);

        final Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setMode(SELECTING);
        expectedConfiguration.setStrategies("changed");
        expectedConfiguration.setDebug(false);
        expectedConfiguration.setDisable(false);
        expectedConfiguration.setReport(report);
        expectedConfiguration.setScm(scm);

        // when
        final Configuration actualConfiguration = ConfigurationLoader.load();

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_configuration_file_from_a_particular_location() throws IOException {
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

        Map<String, Object> affectedStrategiesConfig = new HashMap<>();
        affectedStrategiesConfig.put("exclusions", Arrays.asList("org.package.*", "org.arquillian.package.*"));
        affectedStrategiesConfig.put("inclusions",
            Arrays.asList("org.package.exclude.*", "org.arquillian.package.exclude.*"));
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

        final File tempConfigFile = getCustomConfigFile();
        System.setProperty(SMART_TESTING_CONFIG, tempConfigFile.getAbsolutePath());

        // when
        final Configuration actualConfiguration = ConfigurationLoader.load();

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_log_warning_if_directory_is_passed_as_custom_config_file() throws IOException {
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

        final File tempConfigFile = temporaryFolder.newFolder();
        System.setProperty(SMART_TESTING_CONFIG, tempConfigFile.getAbsolutePath());

        // when
        final Configuration actualConfiguration = ConfigurationLoader.load();

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
        assertThat(systemErrOut.getLog())
            .contains("WARN: Smart Testing Extension - " + tempConfigFile.getName() + " is a directory. Using the default configuration or please specify a `yaml` configuration file.");
    }

    private File getCustomConfigFile() throws IOException {
        final File tempConfigFile = temporaryFolder.newFile("custom-config.yml");
        String content = new String(
            Files.readAllBytes(getResourceAsFile("configuration/smart-testing.yml").toPath()));
        Files.write(tempConfigFile.toPath(), content.getBytes());
        return tempConfigFile;
    }
}
