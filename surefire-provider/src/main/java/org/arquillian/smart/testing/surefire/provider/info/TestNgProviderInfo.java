package org.arquillian.smart.testing.surefire.provider.info;

import org.arquillian.smart.testing.surefire.provider.ProviderParametersParser;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public final class TestNgProviderInfo implements ProviderInfo {

    private ProviderParametersParser paramParser;

    public TestNgProviderInfo(ProviderParametersParser paramParser) {
        this.paramParser = paramParser;
    }

    public String getProviderClassName() {
        return "org.apache.maven.surefire.testng.TestNGProvider";
    }

    public boolean isApplicable() {
        return paramParser.getTestNgVersion() != null
            && paramParser.getSurefireBooterVersion() != null;
    }


    public String getDepCoordinates() {
        return "org.apache.maven.surefire:surefire-testng:" + paramParser.getSurefireBooterVersion();
    }
}
