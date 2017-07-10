package org.arquillian.smart.testing.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestRequest;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.spi.JavaSPILoader;

public class SmartTestingSurefireProvider implements SurefireProvider {

    private SurefireProvider surefireProvider;
    private ProviderParametersParser paramParser;
    private Class<SurefireProvider> providerClass;
    private ProviderParameters bootParams;
    private TestRequest testRequest;

    public SmartTestingSurefireProvider(ProviderParameters bootParams) {
        this.bootParams = bootParams;
        this.paramParser = new ProviderParametersParser(this.bootParams);
        this.providerClass = new ProviderList(this.paramParser).resolve();
        this.surefireProvider = createSurefireProviderInstance();
        this.testRequest = this.bootParams.getTestRequest();
    }

    private TestsToRun getTestsToRun() {
        final TestsToRun testsToRun = (TestsToRun) getSuites();

        final String strategiesParam = paramParser.getProperty("strategies");

        final String[] strategies = strategiesParam.trim().split("\\s*,\\s*");

        final TestExecutionPlannerLoader testExecutionPlannerLoader =
            new TestExecutionPlannerLoader(new JavaSPILoader(), testRequest.getTestSourceDirectory());

        return new TestStrategyApplier(testsToRun, paramParser,
            testExecutionPlannerLoader, bootParams).apply(Arrays.asList(strategies));
    }

    public Iterable<Class<?>> getSuites() {
        return surefireProvider.getSuites();
    }

    public RunResult invoke(Object forkTestSet)
        throws TestSetFailedException, ReporterException, InvocationTargetException {
        TestsToRun orderedTests = getTestsToRun();
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
