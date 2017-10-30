package org.arquillian.smart.testing.hub.storage.local;

public class TemporaryInternalFiles {

    private static final String SMART_TESTING_SCM_CHANGES = "scm-changes";
    private static final String JUNIT_5_PLATFORM_VERSION = "junit5PlatformVersion";

    public static String getScmChangesFileName(){
        return SMART_TESTING_SCM_CHANGES;
    }

    public static String getJunit5PlatformVersionFileName(String pluginArtifactId){
        return pluginArtifactId + "_" + JUNIT_5_PLATFORM_VERSION;
    }
}
