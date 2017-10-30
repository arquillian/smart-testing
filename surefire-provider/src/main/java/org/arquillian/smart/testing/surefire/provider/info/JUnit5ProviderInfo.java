package org.arquillian.smart.testing.surefire.provider.info;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.surefire.provider.LoaderVersionExtractor;

import static org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles.getJunit5PlatformVersionFileName;

public class JUnit5ProviderInfo implements ProviderInfo {

    private String junit5SurefirePlatformVersion;

    public JUnit5ProviderInfo(File projectDir) {
        junit5SurefirePlatformVersion = retrieveJunit5SurefirePlatformVersion(projectDir, LoaderVersionExtractor.getFailsafePluginVersion());
    }

    public String getProviderClassName() {
        return "org.junit.platform.surefire.provider.JUnitPlatformProvider";
    }

    public boolean isApplicable() {
        return junit5SurefirePlatformVersion != null;
    }

    public String getDepCoordinates() {
        return "org.junit.platform:junit-platform-surefire-provider:" + junit5SurefirePlatformVersion;
    }

    public ProviderParameters convertProviderParameters(ProviderParameters providerParameters) {
        return providerParameters;
    }

    String retrieveJunit5SurefirePlatformVersion(File projectDir, String failsafePluginVersion) {

        final boolean isFailsafePlugin = failsafePluginVersion != null;
        final String prefix = isFailsafePlugin ? LoaderVersionExtractor.ARTIFACT_ID_MAVEN_FAILSAFE_PLUGIN : LoaderVersionExtractor.ARTIFACT_ID_MAVEN_SUREFIRE_PLUGIN;

        final Path junit5PlatformVersionFile = new LocalStorage(projectDir).duringExecution()
            .temporary()
            .file(getJunit5PlatformVersionFileName(prefix))
            .getPath();

        if (Files.exists(junit5PlatformVersionFile)) {
            try {
                final byte[] content = Files.readAllBytes(junit5PlatformVersionFile);
                return new String(content);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

}
