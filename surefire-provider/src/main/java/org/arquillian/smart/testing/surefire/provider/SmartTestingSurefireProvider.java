package org.arquillian.smart.testing.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.ClassNameExtractor;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.spi.JavaSPILoader;

// TODO figure out how to inject our services here
public class SmartTestingSurefireProvider implements SurefireProvider {

    private SurefireProvider surefireProvider;
    private ProviderParametersParser paramParser;
    private SurefireProviderFactory surefireProviderFactory;
    private ProviderParameters bootParams;

    public SmartTestingSurefireProvider(ProviderParameters bootParams) {
        this.bootParams = bootParams;
        this.paramParser = new ProviderParametersParser(this.bootParams);
        this.surefireProviderFactory = new SurefireProviderFactory(this.paramParser);
        this.surefireProvider = surefireProviderFactory.createInstance();
    }

    private TestsToRun getTestsToRun() {
        final TestsToRun testsToRun = (TestsToRun) getSuites();
        final Configuration configuration = Configuration.read();

        final TestExecutionPlannerLoader testExecutionPlannerLoader =
            new TestExecutionPlannerLoader(new JavaSPILoader(), resource -> {
                final String className = new ClassNameExtractor().extractFullyQualifiedName(resource);
                return testsToRun.getClassByName(className) != null;
            }, getGlobPatterns());

        return new TestStrategyApplier(testsToRun, testExecutionPlannerLoader, bootParams.getTestClassLoader()).apply(
            configuration);
    }

    private String[] getGlobPatterns() { // FIXME(affected) Only temporarily for affected
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

        final TestsToRun orderedTests = getTestsToRun();
        if (orderedTests.containsExactly(0)) {
            orderedTests.markTestSetFinished();
            return RunResult.noTestsRun();
        }
        surefireProvider = surefireProviderFactory.createInstance();
        return surefireProvider.invoke(orderedTests);
    }

    public void cancel() {
        surefireProvider.cancel();
    }
}
