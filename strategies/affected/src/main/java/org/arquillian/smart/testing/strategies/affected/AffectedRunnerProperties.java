package org.arquillian.smart.testing.strategies.affected;

public class AffectedRunnerProperties {

    public static final String SMART_TESTING_DEPTH_LEVEL = "smart.testing.depth.level";

    public static final int getSmartTestingDepthLevel() {
        return Integer.parseInt(System.getProperty(SMART_TESTING_DEPTH_LEVEL, "1"));
    }

}
