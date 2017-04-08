package org.arquillian.smart.testing.surefire.provider.info;

import org.arquillian.smart.testing.surefire.provider.ProviderParametersParser;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public final class JUnit4ProviderInfo extends JUnitProviderInfo {

    private ProviderParametersParser paramParser;

    public JUnit4ProviderInfo(ProviderParametersParser paramParser) {
        super(paramParser.getJunitVersion());
        this.paramParser = paramParser;
    }

    public String getProviderClassName() {
        return "org.apache.maven.surefire.junit4.JUnit4Provider";
    }

    public boolean isApplicable() {
        return getJunitDepArtifact() != null
            && isAnyJunit4(getJunitDepArtifact())
            && paramParser.getSurefireBooterVersion() != null;
    }

    public String getDepCoordinates() {
        return "org.apache.maven.surefire:surefire-junit4:" + paramParser.getSurefireBooterVersion();
    }
}
