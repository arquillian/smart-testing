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

        final Report report = Report.builder()
                .enable(true)
                .name(REPORT_FILE_NAME)
                .dir("target")
            .build();

        final Scm scm = Scm.builder()
                .range(Range.builder().head(HEAD).tail(HEAD + "~3").build())
            .build();

        final Configuration expectedConfiguration = Configuration.builder()
                .mode(SELECTING)
                .strategies("changed")
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
    public void should_load_configuration_with_defaults_and_with_specified_system_properties_if_config_file_is_not_given(){
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SMART_TESTING_REPORT_ENABLE, "true");


        final Report report = Report.builder()
                .enable(true)
                .name(REPORT_FILE_NAME)
                .dir("target")
            .build();

        final Scm scm = Scm.builder()
                .range(Range.builder().head(HEAD).tail(HEAD + "~0").build())
            .build();

        final Configuration expectedConfiguration = Configuration.builder()
                .mode(SELECTING)
                .strategies("changed")
                .debug(false)
                .disable(false)
                .report(report)
                .scm(scm)
            .build();

        // when
        final Configuration actualConfiguration = Configuration.load();

        // then
        Assertions.assertThat(actualConfiguration)
            .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
    }

}
