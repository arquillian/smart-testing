package org.arquillian.smart.testing.surefire.provider.info;

import shaded.org.apache.maven.artifact.versioning.ArtifactVersion;
import shaded.org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.arquillian.smart.testing.surefire.provider.SurefireDependencyResolver;

abstract class JUnitProviderInfo implements ProviderInfo {

    private ArtifactVersion junitDepVersion;

    JUnitProviderInfo(String junitVersion) {
        if (junitVersion != null) {
            junitDepVersion = new DefaultArtifactVersion(junitVersion);
        }
    }

    boolean isAnyJunit4() {
        return SurefireDependencyResolver.isWithinVersionSpec(junitDepVersion, "[4.0,5.0.0)");
    }

    ArtifactVersion getJunitDepVersion() {
        return junitDepVersion;
    }

    @Override
    public ProviderParameters convertProviderParameters(ProviderParameters providerParameters) {
        return providerParameters;
    }
}
