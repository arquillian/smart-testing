package org.arquillian.smart.testing.surefire.provider;


import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.arquillian.smart.testing.surefire.provider.info.ProviderInfo;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class SurefireDependencyResolver {

    public static boolean isWithinVersionSpec(@Nullable Artifact artifact, @Nonnull String versionSpec) {
        if (artifact == null) {
            return false;
        }
        try {
            VersionRange range = VersionRange.createFromVersionSpec(versionSpec);
            try {
                return range.containsVersion(artifact.getSelectedVersion());
            } catch (NullPointerException e) {
                return range.containsVersion(new DefaultArtifactVersion(artifact.getBaseVersion()));
            }
        } catch (InvalidVersionSpecificationException | OverConstrainedVersionException e) {
            throw new RuntimeException("Bug in plugin. Please report with stacktrace");
        }
    }

    public static File[] resolve(ProviderInfo providerInfo) {

        return Maven
            .resolver()
            .resolve(providerInfo.getDepCoordinates())
            .withTransitivity()
            .asFile();
    }

    public static ClassLoader addProviderToClasspath(ProviderInfo providerInfo) {
        if (providerInfo != null) {
            File[] files = resolve(providerInfo);
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            try {
                return new URLClassLoader(toURLs(files), classLoader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static URL[] toURLs(File[] archives) throws Exception {
        URL[] urls = new URL[archives.length];
        for (int i = 0; i < archives.length; i++) {
            urls[i] = archives[i].toURI().toURL();
        }
        return urls;
    }
}
