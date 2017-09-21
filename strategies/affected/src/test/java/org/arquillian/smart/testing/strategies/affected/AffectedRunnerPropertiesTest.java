package org.arquillian.smart.testing.strategies.affected;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AffectedRunnerPropertiesTest {

    @Test
    public void should_load_exclusions_from_configuration_file() {
        // given
        final String csvLocation = AffectedRunnerProperties.class.getResource("/config.properties").getPath();

        // when
        AffectedRunnerProperties affectedRunnerProperties = new AffectedRunnerProperties(csvLocation);
        final String smartTestingAffectedExclusions = affectedRunnerProperties.resolve(null,
            affectedRunnerProperties.getProperty(AffectedRunnerProperties.EXCLUSIONS));

        // then
        assertThat(smartTestingAffectedExclusions).isEqualTo("a, b, c");
    }

    @Test
    public void should_load_inclusions_from_configuration_file() {
        // given
        final String csvLocation = AffectedRunnerProperties.class.getResource("/config.properties").getPath();

        // when
        AffectedRunnerProperties affectedRunnerProperties = new AffectedRunnerProperties(csvLocation);
        affectedRunnerProperties.readFile(csvLocation);
        final String smartTestingAffectedInclusions = affectedRunnerProperties.resolve(null,
            affectedRunnerProperties.getProperty(AffectedRunnerProperties.INCLUSIONS));

        // then
        assertThat(smartTestingAffectedInclusions).isEqualTo("d, e");
    }
}
