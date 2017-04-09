package org.arquillian.smart.testing.surefire.provider.info;

import org.arquillian.smart.testing.surefire.provider.ProviderParametersParser;

public class JUnit4ProviderInfo extends JUnitProviderInfo {

    private ProviderParametersParser paramParser;

    public JUnit4ProviderInfo(ProviderParametersParser paramParser) {
        super(paramParser.getJunitVersion());
        this.paramParser = paramParser;
    }

    public String getProviderClassName() {
        return "org.apache.maven.surefire.junit4.JUnit4Provider";
    }

    public boolean isApplicable() {
        return getJunitDepVersion() != null && isAnyJunit4() && paramParser.getSurefireApiVersion() != null;
    }

    public String getDepCoordinates() {
        return "org.apache.maven.surefire:surefire-junit4:" + paramParser.getSurefireApiVersion();
    }
}
