package org.arquillian.smart.testing.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.RunOrderCalculator;
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
        System.out.println(forkTestSet);
        TestsToRun orderedTests = getOrderedTests();
        nastyNastyHackToInfluenceOurTestOrder(orderedTests);
        return surefireProvider.invoke(orderedTests);
    }

    private void nastyNastyHackToInfluenceOurTestOrder(TestsToRun orderedTests) {
        try {
            SecurityUtils.setFieldValue(surefireProvider.getClass(), surefireProvider, "runOrderCalculator",
                (RunOrderCalculator) scannedClasses -> orderedTests);
            surefireProvider.getSuites();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public void cancel() {
        surefireProvider.cancel();
    }
}
