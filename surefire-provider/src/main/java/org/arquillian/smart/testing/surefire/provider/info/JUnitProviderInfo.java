package org.arquillian.smart.testing.surefire.provider.info;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.arquillian.smart.testing.surefire.provider.SurefireDependencyResolver;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public abstract class JUnitProviderInfo implements ProviderInfo {

    private Artifact junitDepArtifact;

    JUnitProviderInfo(String junitVersion) {
        if (junitVersion != null) {
            junitDepArtifact = createJunitDepArtifact(junitVersion);
        }
    }

    protected boolean isAnyJunit4(Artifact artifact) {
        return SurefireDependencyResolver.isWithinVersionSpec(artifact, "[4.0,)");
    }

    protected Artifact getJunitDepArtifact() {
        return junitDepArtifact;
    }

    private Artifact createJunitDepArtifact(String version) {
        VersionRange fromVersionSpec = null;
        try {
            fromVersionSpec = VersionRange.createFromVersionSpec(version);
        } catch (InvalidVersionSpecificationException e) {
            e.printStackTrace();
        }

        return new DefaultArtifact("junit", "junit", fromVersionSpec, "test", "jar", null, new DefaultArtifactHandler());
    }
}
