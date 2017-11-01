package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.Arrays;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.junit.Before;
import org.junit.Test;

import static org.arquillian.smart.testing.strategies.affected.AffectedTestsDetector.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class AffectedConfigurationTest {

    private AffectedConfiguration affectedConfiguration;

    @Before
    public void loadConfiguration() {
        final String configFile = AffectedConfiguration.class.getResource("/smart-testing.yaml").getFile();
        final Configuration configuration = ConfigurationLoader.load(new File(configFile).getParentFile());
        configuration.loadStrategyConfigurations(AFFECTED);
        affectedConfiguration = (AffectedConfiguration) configuration.getStrategyConfiguration(AFFECTED);
    }

    @Test
    public void should_load_exclusions_from_configuration_file() {
        assertThat(affectedConfiguration.getExclusions()).isEqualTo(Arrays.asList("a", "b", "c"));
    }

    @Test
    public void should_load_inclusions_from_configuration_file() {
        assertThat(affectedConfiguration.getInclusions()).isEqualTo(Arrays.asList("d", "e"));
    }
}
