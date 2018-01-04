package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.Constants.CURRENT_DIR;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_REPORT_ENABLE;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_CONFIG;
import static org.arquillian.smart.testing.configuration.ConfigurationTest.prepareConfigurationForConfigFile;
import static org.arquillian.smart.testing.configuration.ConfigurationTest.prepareDefaultConfiguration;
import static org.arquillian.smart.testing.configuration.ResourceLoader.getResourceAsFile;
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
            ConfigurationLoader.load(getResourceAsFile("configuration/smart-testing-with-lastChanges.yml"));

        // then
        final Range range = actualConfiguration.getScm().getRange();
        assertThat(range).extracting("head", "tail").containsExactly(HEAD, HEAD + "~3");
    }

    @Test
    public void should_load_configuration_for_scmLastChanges_from_config_file() {
        // when
        final Configuration actualConfiguration =
            ConfigurationLoader.load(getResourceAsFile("configuration/smart-testing-with-lastChanges.yml"));

        // then
        final Range range = actualConfiguration.getScm().getRange();
        assertThat(range).extracting("head", "tail").containsExactly(HEAD, HEAD + "~1");
    }

    @Test
    public void should_load_configuration_for_rangeHead_and_rangeTail_from_config_file() {
        // when
        final Configuration actualConfiguration =
            ConfigurationLoader.load(getResourceAsFile("configuration/smart-testing.yml"));

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
            ConfigurationLoader.load(getResourceAsFile("configuration/smart-testing.yml"));

        // then
        final Range range = actualConfiguration.getScm().getRange();
        assertThat(range).extracting("head", "tail").containsExactly(HEAD + "~1", HEAD + "~5");
    }

    @Test
    public void should_load_configuration_with_overwriting_system_properties_over_properties_from_config_file() {
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SCM_RANGE_TAIL, HEAD + "~4");
        System.setProperty("smart.testing.strategy.my", "org.arquillian.smart.testing:strategy-my:1.0.0");

        final Configuration expectedConfiguration = prepareConfigurationForConfigFile();
        expectedConfiguration.setStrategies("changed");
        expectedConfiguration.getScm().setLastChanges("4");

        List<String> customStrategies = new ArrayList<>();
        Collections.addAll(customStrategies, expectedConfiguration.getCustomStrategies());
        customStrategies.add("smart.testing.strategy.my=org.arquillian.smart.testing:strategy-my:1.0.0");
        expectedConfiguration.setCustomStrategies(customStrategies.toArray(new String[customStrategies.size()]));

        // when
        final Configuration actualConfiguration =
            ConfigurationLoader.load(getResourceAsFile("configuration/smart-testing.yml"));

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_configuration_with_defaults_and_with_specified_system_properties_if_config_file_is_not_given() {
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SMART_TESTING_REPORT_ENABLE, "true");

        final Configuration expectedConfiguration = prepareDefaultConfiguration();
        expectedConfiguration.setStrategies("changed");
        expectedConfiguration.getReport().setEnable(true);

        // when
        final Configuration actualConfiguration = ConfigurationLoader.load(CURRENT_DIR);

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_configuration_file_from_a_particular_location() throws IOException {
        // given
        final File tempConfigFile = getCustomConfigFile();
        System.setProperty(SMART_TESTING_CONFIG, tempConfigFile.getAbsolutePath());

        final Configuration expectedConfiguration = prepareConfigurationForConfigFile();

        // when
        final Configuration actualConfiguration = ConfigurationLoader.load(CURRENT_DIR);

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_log_warning_if_directory_is_passed_as_custom_config_file() throws IOException {
        // given
        final File tempConfigFile = temporaryFolder.newFolder();
        System.setProperty(SMART_TESTING_CONFIG, tempConfigFile.getAbsolutePath());

        // when
        ConfigurationLoader.load(CURRENT_DIR);

        // then
        assertThat(systemErrOut.getLog())
            .contains("WARN: Smart Testing Extension - " + tempConfigFile.getName() + " is a directory. Using the default configuration file resolution.");
    }

    private File getCustomConfigFile() throws IOException {
        final File tempConfigFile = temporaryFolder.newFile("custom-config.yml");
        String content = new String(
            Files.readAllBytes(getResourceAsFile("configuration/smart-testing.yml").toPath()));
        Files.write(tempConfigFile.toPath(), content.getBytes());
        return tempConfigFile;
    }
}
