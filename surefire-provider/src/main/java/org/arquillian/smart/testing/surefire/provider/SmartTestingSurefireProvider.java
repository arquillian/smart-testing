package org.arquillian.smart.testing.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;

public class SmartTestingSurefireProvider implements SurefireProvider {

    private SurefireProvider surefireProvider;
    private ProviderParametersParser paramParser;
    Class<SurefireProvider> providerClass;
    ProviderParameters bootParams;

    public SmartTestingSurefireProvider(ProviderParameters bootParams) {
        paramParser = new ProviderParametersParser(bootParams);
        this.bootParams = bootParams;
        providerClass = new ProviderList(paramParser).resolve();
        surefireProvider = createSurefireProviderInstance();
    }

    private TestsToRun getOrderedTests() {
        TestsToRun testsToRun = (TestsToRun) getSuites();

        String orderStrategyParam = paramParser.getProperty("orderStrategy");
        String[] orderStrategy = orderStrategyParam.split(",");

        return new TestStrategyApplier(testsToRun, new TestExecutionPlannerLoader(new JavaSPILoader() {
            @Override
            public <S> Iterable<S> load(Class<S> service) {
                return ServiceLoader.load(service);
            }
        }, getGlobPatterns())).apply(Arrays.asList(orderStrategy));
    }
    private String[] getGlobPatterns() {
        final List<String> globPatterns = paramParser.getIncludes();
        // TODO question why exclusions are added too?
        globPatterns.addAll(paramParser.getExcludes());
        return globPatterns.toArray(new String[globPatterns.size()]);
    }

    public Iterable<Class<?>> getSuites() {
        return surefireProvider.getSuites();
    }

    public RunResult invoke(Object forkTestSet)
        throws TestSetFailedException, ReporterException, InvocationTargetException {
        TestsToRun orderedTests = getOrderedTests();
        surefireProvider = createSurefireProviderInstance();
        return surefireProvider.invoke(orderedTests);
    }

    private SurefireProvider createSurefireProviderInstance(){
        return SecurityUtils.newInstance(providerClass, new Class[] {ProviderParameters.class}, new Object[] {bootParams});
    }

    public void cancel() {
        surefireProvider.cancel();
    }
}
