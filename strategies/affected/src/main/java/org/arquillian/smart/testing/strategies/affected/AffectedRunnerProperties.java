package org.arquillian.smart.testing.strategies.affected;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.StringJoiner;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

class AffectedRunnerProperties {

    static final String SMART_TESTING_AFFECTED_CONFIG = "smart.testing.affected.config";

    static final String SMART_TESTING_AFFECTED_TRANSITIVITY = "smart.testing.affected.transitivity";
    @SuppressWarnings("unused")
    static final String DEFAULT_SMART_TESTING_AFFECTED_TRANSITIVITY_VALUE = "true";

    static final String SMART_TESTING_AFFECTED_EXCLUSIONS = "smart.testing.affected.exclusions";
    static final String SMART_TESTING_AFFECTED_INCLUSIONS = "smart.testing.affected.inclusions";
    static final String INCLUSIONS = "inclusions";
    static final String EXCLUSIONS = "exclusions";
    private static final String AFFECTED = "affected";

    private final Properties properties = new Properties();
    private AffectedConfiguration affectedConfiguration;

    AffectedRunnerProperties(Configuration configuration) {
        final StrategyConfiguration affectedConfig = configuration.getStrategyConfiguration(AFFECTED);
        if (affectedConfig == null) {
            return;
        }
        affectedConfiguration = (AffectedConfiguration) affectedConfig;
        readFile(affectedConfiguration.getConfig());
    }

    AffectedRunnerProperties(String csvLocation) {
        readFile(csvLocation);
    }

    void readFile(String location) {
        if (location == null) {
            return;
        }

        try {
            properties.load(Files.newBufferedReader(Paths.get(location)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    boolean getSmartTestingAffectedTransitivity() {
        return affectedConfiguration != null && affectedConfiguration.isTransitivity();
    }

    String getSmartTestingAffectedExclusions() {
        if (affectedConfiguration == null) {
            return null;
        }
        String exclusions = affectedConfiguration.getExclusions();
        String exclusionsFromFile = properties.getProperty(EXCLUSIONS);

        return resolve(exclusions, exclusionsFromFile);
    }

    String getSmartTestingAffectedInclusions() {
        if (affectedConfiguration == null) {
            return null;
        }
        String inclusions = affectedConfiguration.getInclusions();
        String inclusionsFromFile = properties.getProperty(INCLUSIONS);

        return resolve(inclusions, inclusionsFromFile);
    }

    String resolve(String expressions, String fileExpressions) {
        StringJoiner joiner = new StringJoiner(", ");
        if (expressions != null) {
            joiner.add(expressions);
        }

        if (fileExpressions != null) {
            joiner.add(fileExpressions);
        }

        return joiner.toString().trim();
    }

    String getProperty(String key) {
        return properties.getProperty(key);
    }
}
