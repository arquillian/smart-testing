package org.arquillian.smart.testing.surefire.provider.info;

import org.arquillian.smart.testing.surefire.provider.ProviderParametersParser;

public class TestNgProviderInfo implements ProviderInfo {

    private ProviderParametersParser paramParser;

    public TestNgProviderInfo(ProviderParametersParser paramParser) {
        this.paramParser = paramParser;
    }

    public String getProviderClassName() {
        return "org.apache.maven.surefire.testng.TestNGProvider";

    }

    public boolean isApplicable() {
        return paramParser.getTestNgVersion() != null
            && paramParser.getSurefireApiVersion() != null;
    }

    public String getDepCoordinates() {
        return "org.apache.maven.surefire:surefire-testng:" + paramParser.getSurefireApiVersion();
    }
}
