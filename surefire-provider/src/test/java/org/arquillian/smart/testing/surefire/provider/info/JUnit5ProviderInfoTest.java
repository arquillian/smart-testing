package org.arquillian.smart.testing.surefire.provider.info;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.arquillian.smart.testing.surefire.provider.LoaderVersionExtractor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles.getJunit5PlatformVersionFileName;
import static org.assertj.core.api.Assertions.assertThat;

public class JUnit5ProviderInfoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_read_junit_5_platform_version_when_failsafe_used() throws IOException {
        // given
        createJUnit5PlatformVersion(LoaderVersionExtractor.ARTIFACT_ID_MAVEN_FAILSAFE_PLUGIN);

        JUnit5ProviderInfo jUnit5ProviderInfo = new JUnit5ProviderInfo(this.temporaryFolder.getRoot());

        // when
        final String junit5PlatformVersion = jUnit5ProviderInfo.retrieveJunit5SurefirePlatformVersion(
            this.temporaryFolder.getRoot(), "2.20");

        // then
        assertThat(junit5PlatformVersion).isEqualTo("1.0.1");

    }

    @Test
    public void should_read_junit_5_platform_version_when_surefire_used() throws IOException {
        // given
        createJUnit5PlatformVersion(LoaderVersionExtractor.ARTIFACT_ID_MAVEN_SUREFIRE_PLUGIN);

        JUnit5ProviderInfo jUnit5ProviderInfo = new JUnit5ProviderInfo(this.temporaryFolder.getRoot());

        // when
        final String junit5PlatformVersion = jUnit5ProviderInfo.retrieveJunit5SurefirePlatformVersion(
            this.temporaryFolder.getRoot(), null);

        // then
        assertThat(junit5PlatformVersion).isEqualTo("1.0.1");

    }

    private void createJUnit5PlatformVersion(String prefix) throws IOException {
        final File temporaryFolder = this.temporaryFolder.newFolder(".smart-testing", "temporary");
        final File versionFile = new File(temporaryFolder, getJunit5PlatformVersionFileName(prefix));
        Files.write(versionFile.toPath(), "1.0.1".getBytes());
    }

}
