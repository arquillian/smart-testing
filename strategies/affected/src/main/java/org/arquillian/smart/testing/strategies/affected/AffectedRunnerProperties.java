package org.arquillian.smart.testing.strategies.affected;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.StringJoiner;

class AffectedRunnerProperties {

    private static final String SMART_TESTING_AFFECTED_CONFIG = "smart.testing.affected.config";

    static final String SMART_TESTING_AFFECTED_TRANSITIVITY = "smart.testing.affected.transitivity";
    static final String DEFAULT_SMART_TESTING_AFFECTED_TRANSITIVITY_VALUE = "true";

    static final String SMART_TESTING_AFFECTED_EXCLUSIONS = "smart.testing.affected.exclusions";
    static final String SMART_TESTING_AFFECTED_INCLUSIONS = "smart.testing.affected.inclusions";
    static final String INCLUSIONS = "inclusions";
    static final String EXCLUSIONS = "exclusions";

    private final Properties properties = new Properties();

    AffectedRunnerProperties(){
        readFile(System.getProperty(SMART_TESTING_AFFECTED_CONFIG));
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
        return Boolean.parseBoolean(System.getProperty(SMART_TESTING_AFFECTED_TRANSITIVITY,
            DEFAULT_SMART_TESTING_AFFECTED_TRANSITIVITY_VALUE));
    }

    String getSmartTestingAffectedExclusions() {
        String exclusions = System.getProperty(SMART_TESTING_AFFECTED_EXCLUSIONS);
        String exclusionsFromFile = properties.getProperty(EXCLUSIONS);

        return resolve(exclusions, exclusionsFromFile);
    }

    String getSmartTestingAffectedInclusions() {
        String inclusions = System.getProperty(SMART_TESTING_AFFECTED_INCLUSIONS);
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

    String getProperty(String key){
        return properties.getProperty(key);
    }

}
