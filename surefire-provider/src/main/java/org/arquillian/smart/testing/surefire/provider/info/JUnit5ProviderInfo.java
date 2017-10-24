package org.arquillian.smart.testing.surefire.provider.info;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.surefire.provider.LoaderVersionExtractor;

public class JUnit5ProviderInfo extends JUnitProviderInfo {

    private String junit5SurefirePlatformVersion;

    public JUnit5ProviderInfo() {
        super(LoaderVersionExtractor.getJUnit5Version());
        junit5SurefirePlatformVersion = retrieveJunit5SurefirePlatformVersion();
    }

    public String getProviderClassName() {
        return "org.junit.platform.surefire.provider.JUnitPlatformProvider";
    }

    public boolean isApplicable() {
        return getJunitDepVersion() != null && isAnyJunit5() && junit5SurefirePlatformVersion != null;
    }

    public String getDepCoordinates() {
        return "org.junit.platform:junit-platform-surefire-provider:" + junit5SurefirePlatformVersion;
    }

    private String retrieveJunit5SurefirePlatformVersion() {

        final Path junit5PlatformVersionFile = new LocalStorage(Paths.get("").toFile()).duringExecution()
            .temporary()
            .file("junit5PlatformVersion")
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
