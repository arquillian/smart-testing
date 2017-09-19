package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import org.apache.maven.surefire.cli.CommandLineOption;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.SmartTesting;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.logger.DefaultLoggerFactory;
import org.arquillian.smart.testing.logger.Log;

import static org.apache.maven.surefire.util.TestsToRun.fromClass;

// TODO figure out how to inject our services here
public class SmartTestingSurefireProvider implements SurefireProvider {

    private SurefireProvider surefireProvider;
    private final ProviderParametersParser paramParser;
    private final SurefireProviderFactory surefireProviderFactory;
    private final ProviderParameters bootParams;

    @SuppressWarnings("unused") // Used by Surefire Core
    public SmartTestingSurefireProvider(ProviderParameters bootParams) {
        this.bootParams = bootParams;
        this.paramParser = new ProviderParametersParser(this.bootParams);
        this.surefireProviderFactory = new SurefireProviderFactory(this.paramParser);
        this.surefireProvider = surefireProviderFactory.createInstance();
        Log.setLoggerFactory(new DefaultLoggerFactory(isAnyDebugEnabled()));
    }

    SmartTestingSurefireProvider(ProviderParameters bootParams, SurefireProviderFactory surefireProviderFactory) {
        this.bootParams = bootParams;
        this.paramParser = new ProviderParametersParser(this.bootParams);
        this.surefireProviderFactory = surefireProviderFactory;
        this.surefireProvider = surefireProviderFactory.createInstance();
        Log.setLoggerFactory(new DefaultLoggerFactory(isAnyDebugEnabled()));
    }

    public Iterable<Class<?>> getSuites() {
        return getOptimizedTestsToRun((TestsToRun) surefireProvider.getSuites());
    }

    public RunResult invoke(Object forkTestSet) throws TestSetFailedException, ReporterException, InvocationTargetException {
        final TestsToRun orderedTests = getTestsToRun(forkTestSet);
        this.surefireProvider = surefireProviderFactory.createInstance();
        return surefireProvider.invoke(orderedTests);
    }

    public void cancel() {
        surefireProvider.cancel();
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

    private TestsToRun getOptimizedTestsToRun(TestsToRun testsToRun) {
        Set<TestSelection> selection = SmartTesting
            .with(className -> testsToRun.getClassByName(className) != null, Configuration.loadPrecalculated(getProjectDir()))
            .in(getProjectDir())
            .applyOnClasses(testsToRun);

        return new TestsToRun(SmartTesting.getClasses(selection));
    }

    private File getProjectDir() {
        if (System.getProperty("basedir") == null) {
            final File testSourceDirectory = bootParams.getTestRequest().getTestSourceDirectory();
            return findFirstMatchingPom(testSourceDirectory);
        } else {
            return new File(System.getProperty("basedir"));
        }
    }

    private File findFirstMatchingPom(File source) {
        if (source.isDirectory() && new File(source, "pom.xml").exists()) {
            return source;
        }
        return findFirstMatchingPom(source.getParentFile());
    }

    private boolean isAnyDebugEnabled(){
        return bootParams.getMainCliOptions().contains(CommandLineOption.LOGGING_LEVEL_DEBUG)
            || Configuration.load().isDebug();
    }
}
