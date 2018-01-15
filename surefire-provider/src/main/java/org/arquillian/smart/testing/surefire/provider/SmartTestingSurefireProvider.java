package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.surefire.cli.CommandLineOption;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestListResolver;
import org.apache.maven.surefire.testset.TestRequest;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.SmartTesting;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.logger.DefaultLoggerFactory;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.surefire.provider.logger.SurefireProviderLoggerFactory;

import static org.apache.maven.surefire.util.TestsToRun.fromClass;
import static org.arquillian.smart.testing.surefire.provider.logger.SurefireProviderLoggerFactory.NOT_COMPATIBLE_MESSAGE;

// TODO figure out how to inject our services here
public class SmartTestingSurefireProvider implements SurefireProvider {

    private final ProviderParametersParser paramParser;
    private final SurefireProviderFactory surefireProviderFactory;
    private final ProviderParameters providerParameters;
    private final Configuration configuration;
    private SurefireProvider surefireProvider;
    private List<String> selectedTestMethods = new ArrayList<>();
    private SmartTestingInvoker smartTestingInvoker;

    @SuppressWarnings("unused") // Used by Surefire Core
    public SmartTestingSurefireProvider(ProviderParameters providerParameters) {
        this.providerParameters = providerParameters;
        this.paramParser = new ProviderParametersParser(this.providerParameters);
        final File projectDir = getProjectDir();
        this.surefireProviderFactory = new SurefireProviderFactory(this.paramParser, projectDir);
        this.surefireProvider = surefireProviderFactory.createInstance(providerParameters);
        this.configuration = ConfigurationLoader.loadPrecalculated(projectDir);
        Log.setLoggerFactory(new SurefireProviderLoggerFactory(getConsoleLogger(), isAnyDebugEnabled()));
        this.smartTestingInvoker = new SmartTestingInvoker();
    }

    SmartTestingSurefireProvider(ProviderParameters providerParameters, SurefireProviderFactory surefireProviderFactory,
        SmartTestingInvoker smartTestingInvoker) {
        this.providerParameters = providerParameters;
        this.paramParser = new ProviderParametersParser(this.providerParameters);
        this.surefireProviderFactory = surefireProviderFactory;
        this.surefireProvider = surefireProviderFactory.createInstance(providerParameters);
        this.configuration = ConfigurationLoader.loadPrecalculated(getProjectDir());
        Log.setLoggerFactory(new SurefireProviderLoggerFactory(getConsoleLogger(), isAnyDebugEnabled()));
        this.smartTestingInvoker = smartTestingInvoker;
    }

    SmartTestingSurefireProvider(ProviderParameters providerParameters, SurefireProviderFactory surefireProviderFactory) {
        this(providerParameters, surefireProviderFactory, new SmartTestingInvoker());
    }

    public Iterable<Class<?>> getSuites() {
        return getOptimizedTestsToRun((TestsToRun) surefireProvider.getSuites());
    }

    public RunResult invoke(Object forkTestSet) throws TestSetFailedException, ReporterException, InvocationTargetException {
        final TestsToRun orderedTests = getTestsToRun(forkTestSet);

        if (!selectedTestMethods.isEmpty()) {
            setTestMethodSelectionToParams();
        }

        this.surefireProvider = surefireProviderFactory.createInstance(providerParameters);
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
        Set<TestSelection> selection =
            smartTestingInvoker.invokeSmartTestingAPI(testsToRun, configuration, getProjectDir());

        if (containsAnyMethodSelection(selection)) {
            selectedTestMethods = selection.stream()
                .flatMap(this::createTestMethodsSelection)
                .collect(Collectors.toList());
        }

        return new TestsToRun(SmartTesting.getClasses(selection));
    }

    private boolean containsAnyMethodSelection(Set<TestSelection> selection) {
        return selection.stream()
            .anyMatch(testSelection -> !testSelection.getTestMethodNames().isEmpty());
    }

    private Stream<String> createTestMethodsSelection(TestSelection testSelection) {
        if (testSelection.getTestMethodNames().isEmpty()) {
            return Stream.of(testSelection.getClassName() + "#*");
        } else {
            return testSelection.getTestMethodNames().stream()
                .map(methodName -> testSelection.getClassName() + "#" + methodName);
        }
    }

    private File getProjectDir() {
        if (System.getProperty("basedir") == null) {
            final File testSourceDirectory = providerParameters.getTestRequest().getTestSourceDirectory();
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

    private boolean isAnyDebugEnabled() {
        return providerParameters.getMainCliOptions().contains(CommandLineOption.LOGGING_LEVEL_DEBUG)
            || configuration.isDebug();
    }

    private Object getConsoleLogger() {
        try {
            Optional<Method> method = Arrays.stream(providerParameters.getClass().getMethods())
                .filter(this::isGetConsoleLoggerMethod)
                .findFirst();
            if (method.isPresent()) {
                return method.get().invoke(providerParameters);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            new DefaultLoggerFactory()
                .getLogger()
                .warn(NOT_COMPATIBLE_MESSAGE);
        }
        return null;
    }

    private boolean isGetConsoleLoggerMethod(Method method) {
        return method.getName().equals("getConsoleLogger")
            && method.getParameterCount() == 0
            && method.getModifiers() == Modifier.PUBLIC
            && method.getReturnType() != Void.class;
    }

    private void setTestMethodSelectionToParams() {
        TestListResolver originalResolver = providerParameters.getTestRequest().getTestListResolver();
        TestListResolver selectedResolver = new TestListResolver(selectedTestMethods);

        TestListResolver newTestListResolver =
            TestListResolver.newTestListResolver(
                selectedResolver.getIncludedPatterns(),
                originalResolver.getExcludedPatterns());

        try {
            Field requestedTestsField = SecurityUtils.getField(TestRequest.class, "requestedTests");
            if (!requestedTestsField.isAccessible()) {
                requestedTestsField.setAccessible(true);
            }
            requestedTestsField.set(providerParameters.getTestRequest(), newTestListResolver);
        } catch (Exception e) {
            throw new RuntimeException("It wasn't possible to set the value "
                + newTestListResolver
                + " on field 'requestedTests'. Please, don't use the test method selection and report this issue.", e);
        }
    }
}
