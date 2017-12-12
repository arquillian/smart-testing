package org.arquillian.smart.testing.surefire.provider.info;

import org.arquillian.smart.testing.surefire.provider.LoaderVersionExtractor;

import static org.arquillian.smart.testing.known.surefire.providers.KnownProvider.JUNIT_4;

public class JUnit4ProviderInfo extends JUnitProviderInfo {

    public JUnit4ProviderInfo() {
        super(LoaderVersionExtractor.getJunitVersion());
    }

    public String getProviderClassName() {
        return JUNIT_4.getProviderClassName();
    }

    public boolean isApplicable() {
        return getJunitDepVersion() != null && isAnyJunit4() && LoaderVersionExtractor.getSurefireApiVersion() != null;
    }

    public String getDepCoordinates() {
        return String.join(":", JUNIT_4.getGroupId(), JUNIT_4.getArtifactId(),
            LoaderVersionExtractor.getSurefireApiVersion());
    }
}
