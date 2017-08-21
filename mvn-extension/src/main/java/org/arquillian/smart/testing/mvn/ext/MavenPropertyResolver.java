package org.arquillian.smart.testing.mvn.ext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MavenPropertyResolver {

    private static final Pattern TEST_CLASS_PATTERN = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

    static boolean isSkipTestExecutionSet() {
        return isSkipTests() || isSkip();
    }

    static private boolean isSkipTests() {
        return Boolean.valueOf(System.getProperty("skipTests", "false"));
    }

    static boolean isSkipITs() {
        return Boolean.valueOf(System.getProperty("skipITs", "false"));
    }

    static private boolean isSkip() {
        return Boolean.valueOf(System.getProperty("maven.test.skip", "false"));
    }

    static boolean isSpecificTestClassSet() {
        String specificTestClasses = System.getProperty("test");
        return specificTestClasses != null && !containsPattern(specificTestClasses);
    }

    private static boolean containsPattern(String specificTestClasses) {
        Matcher matcher = TEST_CLASS_PATTERN.matcher(specificTestClasses);
        return matcher.find();
    }
}
