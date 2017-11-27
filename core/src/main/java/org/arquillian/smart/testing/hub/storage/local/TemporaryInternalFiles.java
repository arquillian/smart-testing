package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;

public class TemporaryInternalFiles {

    private static final String TEMP_REPORT_DIR = "reports";
    private static final String SMART_TESTING_SCM_CHANGES = "scm-changes";
    private static final String JUNIT_5_PLATFORM_VERSION = "junit5PlatformVersion";

    public static String getScmChangesFileName(){
        return SMART_TESTING_SCM_CHANGES;
    }

    public static String getJunit5PlatformVersionFileName(String pluginArtifactId){
        return pluginArtifactId + "_" + JUNIT_5_PLATFORM_VERSION;
    }

    public String getTestReportDirectoryName(){
        return TEMP_REPORT_DIR;
    }

    public LocalStorageDirectoryAction getTestReportDirectoryAction(String rootDir){
        return getTestReportDirectoryAction(new File(rootDir));
    }

    public LocalStorageDirectoryAction getTestReportDirectoryAction(File rootDir){
        return new LocalStorage(rootDir)
            .duringExecution()
            .temporary()
            .directory(TEMP_REPORT_DIR);
    }
}
