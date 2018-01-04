package org.arquillian.smart.testing.surefire.provider.info;

import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.arquillian.smart.testing.surefire.provider.LoaderVersionExtractor;
import org.arquillian.smart.testing.surefire.provider.custom.assertions.SurefireProviderSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.arquillian.smart.testing.known.surefire.providers.KnownProvider.JUNIT_5;

public class CustomProviderInfoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final SurefireProviderSoftAssertions softly = new SurefireProviderSoftAssertions();

    private final String junit5Coordinates = String.join(":", JUNIT_5.getGroupId(), JUNIT_5.getArtifactId(), "1.0.1");

    @Test
    public void should_read_junit_5_platform_version_when_failsafe_used() throws IOException {
        // given
        createCustomProviderInfoFileWithJunit5(LoaderVersionExtractor.ARTIFACT_ID_MAVEN_FAILSAFE_PLUGIN);
        CustomProviderInfo customProviderInfo = new CustomProviderInfo(temporaryFolder.getRoot());

        // when
        customProviderInfo.retrieveCustomProviderInformation(temporaryFolder.getRoot(), "2.20");

        // then
        softly.assertThat(customProviderInfo)
            .hasDepCoordinates(junit5Coordinates)
            .hasProviderClassName(JUNIT_5.getProviderClassName());
    }

    @Test
    public void should_read_junit_5_platform_version_when_surefire_used() throws IOException {
        // given
        createCustomProviderInfoFileWithJunit5(LoaderVersionExtractor.ARTIFACT_ID_MAVEN_SUREFIRE_PLUGIN);
        CustomProviderInfo customProviderInfo = new CustomProviderInfo(temporaryFolder.getRoot());

        // when
        customProviderInfo.retrieveCustomProviderInformation(temporaryFolder.getRoot(), null);

        // then
        softly.assertThat(customProviderInfo)
            .hasDepCoordinates(junit5Coordinates)
            .hasProviderClassName(JUNIT_5.getProviderClassName());}

    private void createCustomProviderInfoFileWithJunit5(String prefix) throws IOException {
        TemporaryInternalFiles
            .createCustomProvidersDirAction(temporaryFolder.getRoot(), prefix)
            .createWithFile(junit5Coordinates, JUNIT_5.getProviderClassName().getBytes());
    }

}
