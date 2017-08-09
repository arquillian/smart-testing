package org.arquillian.smart.testing.mvn.ext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ApplicablePlugins {

    SUREFIRE("maven-surefire-plugin"),
    FAILSAFE("maven-failsafe-plugin");

    public static final List<String> ARTIFACT_IDS_LIST =
        Arrays.stream(values()).map(ApplicablePlugins::getArtifactId).collect(Collectors.toList());
    private final String artifactId;

    ApplicablePlugins(String artifactId) {
        this.artifactId = artifactId;
    }

    public static boolean contains(String artifactId) {
        return ARTIFACT_IDS_LIST.contains(artifactId);
    }

    public String getArtifactId() {
        return artifactId;
    }

    public boolean hasSameArtifactId(String artifactId){
        return getArtifactId().equals(artifactId);
    }
}
