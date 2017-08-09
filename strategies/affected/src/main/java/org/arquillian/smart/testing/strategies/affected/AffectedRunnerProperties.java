package org.arquillian.smart.testing.strategies.affected;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AffectedRunnerProperties {

    public static final String SMART_TESTING_AFFECTED_EXCLUSIONS = "smart.testing.affected.exclusions";
    public static final String SMART_TESTING_AFFECTED_INCLUSIONS = "smart.testing.affected.inclusions";
    public static final String SMART_TESTING_AFFECTED_CONFIG = "smart.testing.affected.config";
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
            throw new IllegalStateException(e);
        }
    }

    public static final String getSmartTestingAffectedExclusions() {
        String exclusions = System.getProperty(SMART_TESTING_AFFECTED_EXCLUSIONS);
        String excusionsFromFile = properties.getProperty(EXCLUSIONS);

        return resolve(exclusions, excusionsFromFile);
    }

    public static final String getSmartTestingAffectedInclusions() {
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
