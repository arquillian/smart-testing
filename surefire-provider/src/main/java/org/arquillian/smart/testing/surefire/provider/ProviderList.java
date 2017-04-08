package org.arquillian.smart.testing.surefire.provider;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.arquillian.smart.testing.surefire.provider.info.JUnit4ProviderInfo;
import org.arquillian.smart.testing.surefire.provider.info.JUnitCoreProviderInfo;
import org.arquillian.smart.testing.surefire.provider.info.ProviderInfo;
import org.arquillian.smart.testing.surefire.provider.info.TestNgProviderInfo;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class ProviderList {

    private final Log log = new SystemStreamLog();

    private final ProviderInfo[] wellKnownProviders;

    ProviderParametersParser paramParser;

    ProviderList(ProviderParameters providerParameters) {
        paramParser = new ProviderParametersParser(providerParameters);

        wellKnownProviders = new ProviderInfo[] {
            new TestNgProviderInfo(paramParser),
            new JUnitCoreProviderInfo(paramParser),
            new JUnit4ProviderInfo(paramParser)};
    }

    @SuppressWarnings("unchecked")
    Class<SurefireProvider> resolve() {

        ProviderInfo providerInfo = autoDetectOneProvider();
        try {
            ClassLoader classLoader = SurefireDependencyResolver.addProviderToClasspath(providerInfo);
            if (classLoader != null) {
                return (Class<SurefireProvider>) classLoader.loadClass(providerInfo.getProviderClassName());
            }
            return null;

        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private ProviderInfo autoDetectOneProvider() {
        for (ProviderInfo wellKnownProvider : wellKnownProviders) {
            if (wellKnownProvider.isApplicable()) {
                return wellKnownProvider;
            }
        }
        return null;
    }
}
