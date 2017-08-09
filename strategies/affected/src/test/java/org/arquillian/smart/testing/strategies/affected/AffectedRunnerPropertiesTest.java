package org.arquillian.smart.testing.strategies.affected;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AffectedRunnerPropertiesTest {

    @Test
    public void should_load_exclusions_from_configuration_file() {
        // given
        final String csvLocation = AffectedRunnerProperties.class.getResource("/config.properties").getPath();

        // when
        AffectedRunnerProperties.readFile(csvLocation);
        final String smartTestingAffectedExclusions = AffectedRunnerProperties.resolve(null,
            AffectedRunnerProperties.properties.getProperty(AffectedRunnerProperties.EXCLUSIONS));

        // then
        assertThat(smartTestingAffectedExclusions).isEqualTo("a, b, c");
    }

    @Test
    public void should_load_inclusions_from_configuration_file() {
        // given
        final String csvLocation = AffectedRunnerProperties.class.getResource("/config.properties").getPath();

        // when
        AffectedRunnerProperties.readFile(csvLocation);
        final String smartTestingAffectedInclusions = AffectedRunnerProperties.resolve(null,
            AffectedRunnerProperties.properties.getProperty(AffectedRunnerProperties.INCLUSIONS));

        // then
        assertThat(smartTestingAffectedInclusions).isEqualTo("d, e");
    }
}
