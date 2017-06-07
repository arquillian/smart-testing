package org.arquillian.smart.testing.surefire.provider.info;

import org.arquillian.smart.testing.surefire.provider.LoaderVersionExtractor;

public class JUnit4ProviderInfo extends JUnitProviderInfo {

    public JUnit4ProviderInfo() {
        super(LoaderVersionExtractor.getJunitVersion());
    }

    public String getProviderClassName() {
        return "org.apache.maven.surefire.junit4.JUnit4Provider";
    }

    public boolean isApplicable() {
        return getJunitDepVersion() != null && isAnyJunit4() && LoaderVersionExtractor.getSurefireBooterVersion() != null;
    }

    public String getDepCoordinates() {
        return "org.apache.maven.surefire:surefire-junit4:" + LoaderVersionExtractor.getSurefireBooterVersion();
    }
}
