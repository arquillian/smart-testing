package org.arquillian.smart.testing.strategies.affected;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.StringJoiner;

class AffectedRunnerProperties {

    private static final String SMART_TESTING_AFFECTED_CONFIG = "smart.testing.affected.config";

    static final String SMART_TESTING_AFFECTED_EXCLUSIONS = "smart.testing.affected.exclusions";
    static final String SMART_TESTING_AFFECTED_INCLUSIONS = "smart.testing.affected.inclusions";
    static final String INCLUSIONS = "inclusions";
    static final String EXCLUSIONS = "exclusions";

    static Properties properties = new Properties();

    // To just read once the configuration file.
    static {
        readFile(System.getProperty(SMART_TESTING_AFFECTED_CONFIG));
    }

    static void readFile(String location) {
        if (location == null) {
            return;
        }

        try {
            properties.load(Files.newBufferedReader(Paths.get(location)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String getSmartTestingAffectedExclusions() {
        String exclusions = System.getProperty(SMART_TESTING_AFFECTED_EXCLUSIONS);
        String exclusionsFromFile = properties.getProperty(EXCLUSIONS);

        return resolve(exclusions, exclusionsFromFile);
    }

    static String getSmartTestingAffectedInclusions() {
        String inclusions = System.getProperty(SMART_TESTING_AFFECTED_INCLUSIONS);
        String inclusionsFromFile = properties.getProperty(INCLUSIONS);

        return resolve(inclusions, inclusionsFromFile);
    }

    static String resolve(String expressions, String fileExpressions) {
        StringJoiner joiner = new StringJoiner(", ");
        if (expressions != null) {
            joiner.add(expressions);
        }

        if (fileExpressions != null) {
            joiner.add(fileExpressions);
        }

        return joiner.toString().trim();
    }

}
