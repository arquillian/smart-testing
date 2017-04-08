package org.arquillian.smart.testing.surefire.provider.info;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.arquillian.smart.testing.surefire.provider.SurefireDependencyResolver;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public abstract class JUnitProviderInfo implements ProviderInfo {

    private ArtifactVersion junitDepVersion;

    JUnitProviderInfo(String junitVersion) {
        if (junitVersion != null) {
            junitDepVersion = new DefaultArtifactVersion(junitVersion);
        }
    }

    protected boolean isAnyJunit4() {
        return SurefireDependencyResolver.isWithinVersionSpec(junitDepVersion, "[4.0,)");
    }

    protected ArtifactVersion getJunitDepVersion() {
        return junitDepVersion;
    }

}
