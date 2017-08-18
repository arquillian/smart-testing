package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.ClassNameExtractor;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.spi.JavaSPILoader;

import static org.apache.maven.surefire.util.TestsToRun.fromClass;

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

    SmartTestingSurefireProvider(ProviderParameters bootParams, SurefireProviderFactory surefireProviderFactory) {
        this.bootParams = bootParams;
        this.paramParser = new ProviderParametersParser(this.bootParams);
        this.surefireProviderFactory = surefireProviderFactory;
        this.surefireProvider = surefireProviderFactory.createInstance();
    }

    private TestsToRun getOptimizedTestsToRun(TestsToRun testsToRun) {
        final Configuration configuration = Configuration.load();

        final TestExecutionPlannerLoader testExecutionPlannerLoader =
            new TestExecutionPlannerLoader(new JavaSPILoader(), resource -> {
                final String className = new ClassNameExtractor().extractFullyQualifiedName(resource);
                return testsToRun.getClassByName(className) != null;
            });

        return new TestStrategyApplier(testsToRun, testExecutionPlannerLoader, bootParams.getTestClassLoader(), getBasedir()).apply(
            configuration);
    }

    private String getBasedir() {
        final String path = this.bootParams.getReporterConfiguration().getReportsDirectory().getPath();
        return path.substring(0, path.indexOf(File.separator + "target"));
    }

    public Iterable<Class<?>> getSuites() {
        Iterable<Class<?>> originalSuites = surefireProvider.getSuites();
        return getOptimizedTestsToRun((TestsToRun) originalSuites);
    }

    public RunResult invoke(Object forkTestSet)
        throws TestSetFailedException, ReporterException, InvocationTargetException {

        final TestsToRun orderedTests = getTestsToRun(forkTestSet);
        if (orderedTests.containsExactly(0)) {
            orderedTests.markTestSetFinished();
            return RunResult.noTestsRun();
        }
        surefireProvider = surefireProviderFactory.createInstance();
        return surefireProvider.invoke(orderedTests);
    }

    private TestsToRun getTestsToRun(Object forkTestSet) throws TestSetFailedException {

        if (forkTestSet instanceof TestsToRun) {
            return (TestsToRun) forkTestSet;
        } else if (forkTestSet instanceof Class) {
            return fromClass((Class<?>) forkTestSet);
        } else {
            return (TestsToRun) getSuites();
        }
    }

    public void cancel() {
        surefireProvider.cancel();
    }
}
