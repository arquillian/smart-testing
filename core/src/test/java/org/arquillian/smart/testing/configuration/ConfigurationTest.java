package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.nio.file.Paths;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.Configuration.loadConfigurationFromFile;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;

public class ConfigurationTest {

    @Test
    public void should_load_configuration_with_default_values_if_property_is_not_specified_in_config_file(){
        // given
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
        expectedConfiguration.setMode(ORDERING);
        expectedConfiguration.setStrategies("new", "changed", "affected");
        expectedConfiguration.setApplyTo("surefire");
        expectedConfiguration.setDebug(true);
        expectedConfiguration.setDisable(false);
        expectedConfiguration.setScm(scm);
        expectedConfiguration.setReport(report);

        // when
        final Configuration actualConfiguration = Configuration.load(Paths.get("src/test/resources/smart-testing.yml"));

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

    @Test
    public void should_load_dumped_configuration_from_file_as_configuration() {
        // given
        final Report report = new Report();
        report.setEnable(false);
        report.setName(REPORT_FILE_NAME);
        report.setDir("target");

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
            loadConfigurationFromFile(new File("src/test/resources/dumped-smart-testing.yml"));

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }
}
