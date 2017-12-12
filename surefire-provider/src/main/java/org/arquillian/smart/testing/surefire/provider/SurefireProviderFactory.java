package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.arquillian.smart.testing.surefire.provider.info.CustomProviderInfo;
import org.arquillian.smart.testing.surefire.provider.info.JUnit4ProviderInfo;
import org.arquillian.smart.testing.surefire.provider.info.JUnitCoreProviderInfo;
import org.arquillian.smart.testing.surefire.provider.info.ProviderInfo;
import org.arquillian.smart.testing.surefire.provider.info.TestNgProviderInfo;

public class SurefireProviderFactory {

    private final ProviderInfo providerInfo;
    private final ProviderParameters providerParameters;
    private final Class<SurefireProvider> surefireProviderClass;

    SurefireProviderFactory(ProviderParametersParser paramParser, File projectDir) {
        providerInfo = detectProvider(paramParser, projectDir);
        providerParameters = paramParser.getProviderParameters();
        surefireProviderClass = loadProviderClass();
    }

    private ProviderInfo detectProvider(
        ProviderParametersParser paramParser, File projectDir) {
        ProviderInfo[] wellKnownProviders = new ProviderInfo[] {
            new CustomProviderInfo(projectDir),
            new TestNgProviderInfo(),
            new JUnitCoreProviderInfo(paramParser),
            new JUnit4ProviderInfo()
        };
        return autoDetectOneProvider(wellKnownProviders);
    }

    public SurefireProvider createInstance() {
        return SecurityUtils.newInstance(surefireProviderClass, new Class[] {ProviderParameters.class},
            new Object[] {providerInfo.convertProviderParameters(providerParameters)});
    }

    @SuppressWarnings("unchecked")
    private Class<SurefireProvider> loadProviderClass() {
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

    private ProviderInfo autoDetectOneProvider(ProviderInfo[] providers) {
        for (ProviderInfo provider : providers) {
            if (provider.isApplicable()) {
                return provider;
            }
        }
        throw new IllegalStateException(
            "No surefire provider implementation has been detected as applicable for your environment.");
    }
}
