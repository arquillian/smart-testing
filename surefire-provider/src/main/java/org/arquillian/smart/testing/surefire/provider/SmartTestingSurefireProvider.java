package org.arquillian.smart.testing.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

public class SmartTestingSurefireProvider implements SurefireProvider {

    private SurefireProvider surefireProvider;

    public SmartTestingSurefireProvider(ProviderParameters bootParams) {
        Class<SurefireProvider> provider = new ProviderList(bootParams).resolve();
        surefireProvider =
            SecurityUtils.newInstance(provider, new Class[] {ProviderParameters.class}, new Object[] {bootParams});
    }

    public Iterable<Class<?>> getSuites() {
        return surefireProvider.getSuites();
    }

    public RunResult invoke(Object forkTestSet)
        throws TestSetFailedException, ReporterException, InvocationTargetException {
        return surefireProvider.invoke(forkTestSet);
    }

    public void cancel() {
        surefireProvider.cancel();
    }
}
