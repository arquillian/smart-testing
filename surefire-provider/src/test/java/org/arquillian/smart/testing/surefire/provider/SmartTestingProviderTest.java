package org.arquillian.smart.testing.surefire.provider;

import net.jcip.annotations.NotThreadSafe;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.DefaultDirectConsoleReporter;
import org.apache.maven.surefire.testset.ResolvedTest;
import org.apache.maven.surefire.testset.TestListResolver;
import org.apache.maven.surefire.testset.TestRequest;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.surefire.provider.custom.assertions.SurefireProviderSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(NotThreadSafe.class)
public class SmartTestingProviderTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final SurefireProviderSoftAssertions softly = new SurefireProviderSoftAssertions();

    private final Set<Class<?>> expectedClassesToRun = new LinkedHashSet<>(asList(ATest.class, BTest.class));
    private ProviderParameters providerParameters;
    private TestRequest testRequest;
    private SurefireProviderFactory providerFactory;

    private SurefireProvider surefireProvider;

    @Before
    public void setupMocksAndDumpConfiguration() throws IOException {
        providerParameters = mock(ProviderParameters.class);

        surefireProvider = mock(SurefireProvider.class);
        when(surefireProvider.getSuites()).thenReturn(new TestsToRun(expectedClassesToRun));

        providerFactory = mock(SurefireProviderFactory.class);
        when(providerFactory.createInstance(Mockito.any())).thenReturn(surefireProvider);

        testRequest = new TestRequest(null, temporaryFolder.getRoot(), TestListResolver.getEmptyTestListResolver());
        when(providerParameters.getTestRequest()).thenReturn(testRequest);

        temporaryFolder.newFile("pom.xml");
        ConfigurationLoader.load(Paths.get("").toFile()).dump(temporaryFolder.getRoot());
        System.setProperty("basedir", temporaryFolder.getRoot().toString());

        when(providerParameters.getConsoleLogger()).thenReturn(new DefaultDirectConsoleReporter(new PrintStream(System.out)));
    }

    @Test
    public void when_get_suites_is_called_then_same_list_of_classes_will_be_returned() {
        // given
        SmartTestingSurefireProvider provider = new SmartTestingSurefireProvider(providerParameters, providerFactory);

        // when
        Iterable<Class<?>> suites = provider.getSuites();

        // then
        verify(surefireProvider, times(1)).getSuites();
        assertThat(suites).containsExactlyElementsOf(expectedClassesToRun);
    }

    @Test
    public void test_when_invoke_is_called_with_null() throws Exception {
        // given
        SmartTestingSurefireProvider provider = new SmartTestingSurefireProvider(providerParameters, providerFactory);

        // when
        provider.invoke(null);

        // then
        verify(surefireProvider, times(1)).getSuites();
        verify(surefireProvider, times(1)).invoke(argThat((ArgumentMatcher<Iterable<Class>>)
            iterable -> iterableContains(iterable, expectedClassesToRun)));
    }

    @Test
    public void test_when_invoke_is_called_with_one_class() throws Exception {
        // given
        SmartTestingSurefireProvider provider = new SmartTestingSurefireProvider(providerParameters, providerFactory);

        // when
        provider.invoke(ATest.class);

        // then
        verify(surefireProvider, times(0)).getSuites();
        verify(surefireProvider, times(1)).invoke(argThat((ArgumentMatcher<Iterable<Class>>)
            iterable -> iterableContains(iterable, new LinkedHashSet(Collections.singletonList(ATest.class)))));
    }

    @Test
    public void test_when_invoke_is_called_with_whole_set_of_classes() throws Exception {
        // given
        SmartTestingSurefireProvider provider = new SmartTestingSurefireProvider(providerParameters, providerFactory);

        // when
        provider.invoke(new TestsToRun(expectedClassesToRun));

        // then
        verify(surefireProvider, times(0)).getSuites();
        verify(surefireProvider, times(1)).invoke(argThat((ArgumentMatcher<Iterable<Class>>)
            iterable -> iterableContains(iterable, expectedClassesToRun)));
    }

    @Test
    public void should_add_test_method_selection_to_test_list_resolver() throws TestSetFailedException, InvocationTargetException {
        // given
        TestSelection strategy =
            new TestSelection(ATest.class.getName(), Arrays.asList("firstMethod", "secondMethod"), "strategy");

        prepareTestListResolver();

        SmartTestingInvoker smartTestingInvoker = prepareSTInvoker(Arrays.asList(strategy));
        SmartTestingSurefireProvider provider =
            new SmartTestingSurefireProvider(providerParameters, providerFactory, smartTestingInvoker);

        // when
        provider.invoke(null);

        // then
        softly.assertThat(testRequest.getTestListResolver())
            .hasMethodPatterns(true)
            .hasIncludedMethodPatterns(true)
            .hasExcludedMethodPatterns(true)
            .includedPatterns()
            .containsExactlyInAnyOrder(
                new ResolvedTest(ATest.class.getName(), "firstMethod", false),
                new ResolvedTest(ATest.class.getName(), "secondMethod", false));

        softly.assertThat(testRequest.getTestListResolver())
            .excludedPatterns()
            .containsExactly(new ResolvedTest(ATest.class.getName(), "thirdMethod", false));
    }

    private SmartTestingInvoker prepareSTInvoker(List<TestSelection> selectionToReturn) {
        SmartTestingInvoker smartTestingInvoker = mock(SmartTestingInvoker.class);
        when(smartTestingInvoker.invokeSmartTestingAPI(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(
            new HashSet<>(selectionToReturn));
        return smartTestingInvoker;
    }

    private void prepareTestListResolver(){
        TestListResolver testListResolver =
            new TestListResolver(Arrays.asList("*Test*", "!" + ATest.class.getName() + "#thirdMethod"));
        testRequest = new TestRequest(null, temporaryFolder.getRoot(), testListResolver);
        when(providerParameters.getTestRequest()).thenReturn(testRequest);
    }

    private boolean iterableContains(Iterable<Class> iterable, Set expectedClasses) {
        List<Class> actualCall = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
        return actualCall.size() == expectedClasses.size() && actualCall.containsAll(expectedClasses);
    }

    private static class ATest {

        void firstMethod() {
        }

        void secondMethod() {
        }

        void thirdMethod() {
        }
    }

    private static class BTest {}
}
