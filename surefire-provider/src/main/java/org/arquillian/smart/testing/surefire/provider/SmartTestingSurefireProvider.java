package org.arquillian.smart.testing.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;

public class SmartTestingSurefireProvider implements SurefireProvider {

    private SurefireProvider surefireProvider;
    private ProviderParametersParser paramParser;

    public SmartTestingSurefireProvider(ProviderParameters bootParams) {
        paramParser = new ProviderParametersParser(bootParams);

        Class<SurefireProvider> provider = new ProviderList(paramParser).resolve();
        surefireProvider =
            SecurityUtils.newInstance(provider, new Class[] {ProviderParameters.class}, new Object[] {bootParams});
    }

    private TestsToRun getOrderedTests() {
        TestsToRun testsToRun = (TestsToRun) getSuites();

        String orderStrategyParam = paramParser.getProperty("orderStrategy");
        String[] orderStrategy = orderStrategyParam.split(",");

        return new TestStrategyApplier(testsToRun, paramParser).apply(Arrays.asList(orderStrategy));
    }

    public Iterable<Class<?>> getSuites() {
        return surefireProvider.getSuites();
    }

    public RunResult invoke(Object forkTestSet)
        throws TestSetFailedException, ReporterException, InvocationTargetException {
        TestsToRun orderedTests = getOrderedTests();
        return surefireProvider.invoke(orderedTests);
    }

    public void cancel() {
        surefireProvider.cancel();
    }
}
