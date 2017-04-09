package org.arquillian.smart.testing.surefire.provider.info;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.arquillian.smart.testing.surefire.provider.SurefireDependencyResolver;

abstract class JUnitProviderInfo implements ProviderInfo {

    private ArtifactVersion junitDepVersion;

    JUnitProviderInfo(String junitVersion) {
        if (junitVersion != null) {
            junitDepVersion = new DefaultArtifactVersion(junitVersion);
        }
    }

    boolean isAnyJunit4() {
        return SurefireDependencyResolver.isWithinVersionSpec(junitDepVersion, "[4.0,)");
    }

    ArtifactVersion getJunitDepVersion() {
        return junitDepVersion;
    }

}
