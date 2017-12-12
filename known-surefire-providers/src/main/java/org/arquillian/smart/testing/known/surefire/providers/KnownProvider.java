package org.arquillian.smart.testing.known.surefire.providers;

public enum KnownProvider {

    JUNIT_4(
        "org.apache.maven.surefire",
        "surefire-junit4",
        "org.apache.maven.surefire.junit4.JUnit4Provider"),

    JUNIT_47(
        "org.apache.maven.surefire",
        "surefire-junit47",
        "org.apache.maven.surefire.junitcore.JUnitCoreProvider"),

    JUNIT_5(
        "org.junit.platform",
        "junit-platform-surefire-provider",
        "org.junit.platform.surefire.provider.JUnitPlatformProvider"),

    TESTNG(
        "org.apache.maven.surefire",
        "surefire-testng",
        "org.apache.maven.surefire.testng.TestNGProvider");

    private final String groupId;
    private final String artifactId;
    private final String providerClassName;

    KnownProvider(String groupId, String artifactId, String providerClassName) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.providerClassName = providerClassName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getProviderClassName() {
        return providerClassName;
    }
}
