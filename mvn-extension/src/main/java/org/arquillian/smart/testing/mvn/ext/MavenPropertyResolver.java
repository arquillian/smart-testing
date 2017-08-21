package org.arquillian.smart.testing.mvn.ext;

class MavenPropertyResolver {

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

}
