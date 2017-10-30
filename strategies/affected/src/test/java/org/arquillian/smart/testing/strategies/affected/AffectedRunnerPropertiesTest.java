package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import org.arquillian.smart.testing.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AffectedRunnerPropertiesTest {

    private Configuration configuration;

    @Before
    public void loadConfiguration() {
        final String configFile = AffectedRunnerProperties.class.getResource("/smart-testing.yaml").getFile();

        configuration = Configuration.load(new File(configFile).getParentFile());
        configuration.loadStrategyConfigurations("affected");
    }
    @Test
    public void should_load_exclusions_from_configuration_file() {
        // when
        AffectedRunnerProperties affectedRunnerProperties = new AffectedRunnerProperties(configuration);

        // then
        assertThat(affectedRunnerProperties.getSmartTestingAffectedExclusions()).isEqualTo("a, b, c");
    }

    @Test
    public void should_load_inclusions_from_configuration_file() {
        // when
        AffectedRunnerProperties affectedRunnerProperties = new AffectedRunnerProperties(configuration);

        // then
        assertThat(affectedRunnerProperties.getSmartTestingAffectedInclusions()).isEqualTo("d, e");
    }
}
