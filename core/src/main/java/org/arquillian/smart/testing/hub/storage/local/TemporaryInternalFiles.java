package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;

public class TemporaryInternalFiles {

    private static final String TEMP_REPORT_DIR = "reports";
    private static final String SMART_TESTING_SCM_CHANGES = "scm-changes";
    private static final String CUSTOM_PROVIDERS_DIRECTORY = "customProviders";

    private TemporaryInternalFiles() {
    }

    public static String getScmChangesFileName(){
        return SMART_TESTING_SCM_CHANGES;
    }

    public static String getCustomProvidersDirName(String pluginArtifactId) {
        return pluginArtifactId + "_" + CUSTOM_PROVIDERS_DIRECTORY;
    }

    public static LocalStorageDirectoryAction createCustomProvidersDirAction(File rootDir,
        String pluginArtifactId) {
        return new LocalStorage(rootDir)
            .duringExecution()
            .temporary()
            .directory(getCustomProvidersDirName(pluginArtifactId));
    }

    public static String getTestReportDirName() {
        return TEMP_REPORT_DIR;
    }

    public static LocalStorageDirectoryAction createTestReportDirAction(File rootDir) {
        return new LocalStorage(rootDir)
            .duringExecution()
            .temporary()
            .directory(TEMP_REPORT_DIR);
    }
}
