package org.arquillian.smart.testing.surefire.provider.info;

import org.arquillian.smart.testing.surefire.provider.LoaderVersionExtractor;

public class TestNgProviderInfo implements ProviderInfo {


    public TestNgProviderInfo() {
    }

    public String getProviderClassName() {
        return "org.apache.maven.surefire.testng.TestNGProvider";
    }

    public boolean isApplicable() {
        return LoaderVersionExtractor.getTestNgVersion() != null
            && LoaderVersionExtractor.getSurefireBooterVersion() != null;
    }

    public String getDepCoordinates() {
        return "org.apache.maven.surefire:surefire-testng:" + LoaderVersionExtractor.getSurefireBooterVersion();
    }
}
