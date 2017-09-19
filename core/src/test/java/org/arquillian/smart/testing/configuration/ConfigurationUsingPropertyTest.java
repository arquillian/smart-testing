package org.arquillian.smart.testing.configuration;

import java.util.Map;
import net.jcip.annotations.NotThreadSafe;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_REPORT_DIR;
import static org.arquillian.smart.testing.configuration.ConfigurationTest.loadConfiguration;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;

@NotThreadSafe
public class ConfigurationUsingPropertyTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ClearSystemProperties clearSystemProperties = new ClearSystemProperties(SMART_TESTING, SMART_TESTING_REPORT_DIR);

    @Test
    public void should_load_configuration_with_overwriting_system_properties_over_properties_from_config_file(){
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SMART_TESTING_REPORT_DIR, "smart-testing");


        final ReportConfiguration reportConfiguration = ReportConfiguration.builder()
                .enable(true)
                .name("smart-testing-report.xml")
                .dir("smart-testing")
            .build();

        final ScmConfiguration scmConfiguration = ScmConfiguration.builder()
                .head(HEAD)
                .tail(HEAD + "~3")
            .build();

        final Configuration expectedConfiguration = Configuration.builder()
                .mode(ORDERING)
                .strategies(new String[] {"changed"})
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
    public void should_load_configuration_with_defaults_and_with_specified_system_properties_if_config_file_is_not_given(){
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SMART_TESTING_REPORT_DIR, "smart-testing");


        final ReportConfiguration reportConfiguration = ReportConfiguration.builder()
            .enable(false)
                .name("smart-testing-report.xml")
                .dir("smart-testing")
            .build();

        final ScmConfiguration scmConfiguration = ScmConfiguration.builder()
                .head(HEAD)
                .tail(HEAD + "~0")
            .build();

        final Configuration expectedConfiguration = Configuration.builder()
                .mode(SELECTING)
                .strategies(new String[] {"changed"})
                .debug(false)
                .disable(false)
                .reportConfiguration(reportConfiguration)
                .scmConfiguration(scmConfiguration)
            .build();

        // when
        final Configuration actualConfiguration = Configuration.load();

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

}
