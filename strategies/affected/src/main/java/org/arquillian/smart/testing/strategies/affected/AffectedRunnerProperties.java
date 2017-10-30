package org.arquillian.smart.testing.strategies.affected;

import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

class AffectedRunnerProperties {

    static final String SMART_TESTING_AFFECTED_TRANSITIVITY = "smart.testing.affected.transitivity";
    @SuppressWarnings("unused")
    static final String DEFAULT_SMART_TESTING_AFFECTED_TRANSITIVITY_VALUE = "true";

    static final String SMART_TESTING_AFFECTED_EXCLUSIONS = "smart.testing.affected.exclusions";
    static final String SMART_TESTING_AFFECTED_INCLUSIONS = "smart.testing.affected.inclusions";
    private static final String AFFECTED = "affected";

    private AffectedConfiguration affectedConfiguration;

    AffectedRunnerProperties(Configuration configuration) {
        final StrategyConfiguration affectedConfig = configuration.getStrategyConfiguration(AFFECTED);
        if (affectedConfig == null) {
            return;
        }
        affectedConfiguration = (AffectedConfiguration) affectedConfig;
    }

    boolean getSmartTestingAffectedTransitivity() {
        return affectedConfiguration != null && affectedConfiguration.isTransitivity();
    }

    String getSmartTestingAffectedExclusions() {
        if (affectedConfiguration == null) {
            return null;
        }

        return affectedConfiguration.getExclusions();
    }

    String getSmartTestingAffectedInclusions() {
        if (affectedConfiguration == null) {
            return null;
        }

        return affectedConfiguration.getInclusions();
    }
}
