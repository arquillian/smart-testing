package org.arquillian.smart.testing.configuration;

import java.nio.file.Paths;
import net.jcip.annotations.NotThreadSafe;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_MODE;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_REPORT_ENABLE;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;

@NotThreadSafe
public class ConfigurationUsingPropertyTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_load_configuration_with_overwriting_system_properties_over_properties_from_config_file(){
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SMART_TESTING_MODE, "selecting");

        final Report report = new Report();
        report.setEnable(true);
        report.setName(REPORT_FILE_NAME);
        report.setDir("target");

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(HEAD + "~3");

        final Scm scm = new Scm();
        scm.setRange(range);

        final Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setMode(SELECTING);
        expectedConfiguration.setStrategies("changed");
        expectedConfiguration.setApplyTo("surefire");
        expectedConfiguration.setDebug(true);
        expectedConfiguration.setDisable(false);
        expectedConfiguration.setReport(report);
        expectedConfiguration.setScm(scm);

        // when
        final Configuration actualConfiguration = Configuration.load(Paths.get("src/test/resources/smart-testing.yml"));

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_configuration_with_defaults_and_with_specified_system_properties_if_config_file_is_not_given(){
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SMART_TESTING_REPORT_ENABLE, "true");

        final Report report = new Report();
        report.setEnable(true);
        report.setName(REPORT_FILE_NAME);
        report.setDir("target");

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
        final Configuration actualConfiguration = Configuration.load();

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

}
