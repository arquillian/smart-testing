package org.arquillian.smart.testing.surefire.provider.info;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.arquillian.smart.testing.surefire.provider.LoaderVersionExtractor;

public class CustomProviderInfo implements ProviderInfo {

    private String gav;
    private String providerClassName;

    public CustomProviderInfo(File projectDir) {
        retrieveCustomProviderInformation(projectDir, LoaderVersionExtractor.getFailsafePluginVersion());
    }

    public String getProviderClassName() {
        return providerClassName;
    }

    public boolean isApplicable() {
        return gav != null && providerClassName != null;
    }

    public String getDepCoordinates() {
        return gav;
    }

    public ProviderParameters convertProviderParameters(ProviderParameters providerParameters) {
        return providerParameters;
    }

    void retrieveCustomProviderInformation(File projectDir, String failsafePluginVersion) {

        final boolean isFailsafePlugin = failsafePluginVersion != null;
        final String prefix = isFailsafePlugin ? LoaderVersionExtractor.ARTIFACT_ID_MAVEN_FAILSAFE_PLUGIN
            : LoaderVersionExtractor.ARTIFACT_ID_MAVEN_SUREFIRE_PLUGIN;

        File dir = TemporaryInternalFiles.createCustomProvidersDirAction(projectDir, prefix).getFile();

        if (dir.exists() && dir.listFiles().length > 0) {
            File customProviderInfoFile = dir.listFiles()[0];
            gav = customProviderInfoFile.getName();
            try {
                providerClassName = new String(Files.readAllBytes(customProviderInfoFile.toPath()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
