package org.arquillian.smart.testing.configuration;

import java.nio.file.Paths;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import static org.arquillian.smart.testing.RunMode.SELECTING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_MODE;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_REPORT_ENABLE;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.TARGET;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_RANGE_TAIL;
import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class ConfigurationUsingPropertyTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_load_configuration_with_overwriting_system_property_for_scmLastChanges_over_values_from_config_file() {
        // given
        System.setProperty(SCM_LAST_CHANGES, "3");

        // when
        final Configuration actualConfiguration =
            Configuration.load(Paths.get("src/test/resources/configuration/smart-testing-with-lastChanges.yml"));

        // then
        final Range range = actualConfiguration.getScm().getRange();
        assertThat(range).extracting("head", "tail").containsExactly(HEAD, HEAD + "~3");
    }

    @Test
    public void should_load_configuration_for_scmLastChanges_from_config_file() {
        // when
        final Configuration actualConfiguration =
            Configuration.load(Paths.get("src/test/resources/configuration/smart-testing-with-lastChanges.yml"));

        // then
        final Range range = actualConfiguration.getScm().getRange();
        assertThat(range).extracting("head", "tail").containsExactly(HEAD, HEAD + "~1");
    }

    @Test
    public void should_load_configuration_for_rangeHead_and_rangeTail_from_config_file() {
        // when
        final Configuration actualConfiguration =
            Configuration.load(Paths.get("src/test/resources/configuration/smart-testing.yml"));

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
            Configuration.load(Paths.get("src/test/resources/configuration/smart-testing.yml"));

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

        final Report report = new Report();
        report.setEnable(true);
        report.setDir(TARGET);
        report.setName(REPORT_FILE_NAME);

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(HEAD + "~4");

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
        expectedConfiguration.setAutocorrect(true);

        // when
        final Configuration actualConfiguration =
            Configuration.load(Paths.get("src/test/resources/configuration/smart-testing.yml"));

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
        final Configuration actualConfiguration = Configuration.load();

        // then
        assertThat(actualConfiguration).isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }
}
