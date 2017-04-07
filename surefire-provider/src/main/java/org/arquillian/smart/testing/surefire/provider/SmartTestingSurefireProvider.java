package org.arquillian.smart.testing.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class SmartTestingSurefireProvider implements SurefireProvider {

    //private JUnitCoreProvider jUnitCoreProvider;

    public SmartTestingSurefireProvider(ProviderParameters bootParams) {
        //new ProviderList().resolve();

        //jUnitCoreProvider = new JUnitCoreProvider(bootParams);
        //new ProviderList().resolve();
    }

    public Iterable<Class<?>> getSuites() {
        //return jUnitCoreProvider.getSuites();
        return null;
    }

    public RunResult invoke(Object forkTestSet)
        throws TestSetFailedException, ReporterException, InvocationTargetException {
        //return jUnitCoreProvider.invoke(forkTestSet);
        return null;
    }

    public void cancel() {
        //jUnitCoreProvider.cancel();
    }
}
