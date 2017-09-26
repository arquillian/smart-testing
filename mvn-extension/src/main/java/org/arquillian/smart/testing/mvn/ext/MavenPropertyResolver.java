package org.arquillian.smart.testing.mvn.ext;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MavenPropertyResolver {

    private static final Pattern TEST_CLASS_PATTERN = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

    private static boolean skipTests = false;

    static boolean isSkipTestExecutionSet() {
        return isSkipTests() || isSkip() || skipTests;
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
        String testClasses = System.getProperty("test");
        return testClasses != null && !containsPattern(testClasses);
    }

    static void setSkipTests(boolean skipTests) {
        MavenPropertyResolver.skipTests = skipTests;
    }

    private static boolean containsPattern(String testClasses) {
        return Arrays.stream(testClasses.split(","))
            .map(TEST_CLASS_PATTERN::matcher)
            .anyMatch(Matcher::find);
    }
}
