package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.testset.TestRequest;
import org.apache.maven.surefire.util.TestsToRun;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.hub.storage.local.FileSystemOperations;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalStorageFileAction;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.yaml.snakeyaml.Yaml;

import static java.util.Arrays.asList;
import static org.arquillian.smart.testing.hub.storage.local.DuringExecutionLocalStorage.SMART_TESTING_WORKING_DIRECTORY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SmartTestingProviderTest {

    private final Set<Class<?>> expectedClassesToRun = new LinkedHashSet<>(asList(ATest.class, BTest.class));
    private ProviderParameters providerParameters;
    private SurefireProviderFactory providerFactory;

    private SurefireProvider surefireProvider;

    @Before
    public void setupMocks() {
        providerParameters = mock(ProviderParameters.class);

        surefireProvider = mock(SurefireProvider.class);
        when(surefireProvider.getSuites()).thenReturn(new TestsToRun(expectedClassesToRun));

        providerFactory = mock(SurefireProviderFactory.class);
        when(providerFactory.createInstance()).thenReturn(surefireProvider);

        TestRequest testRequest = mock(TestRequest.class);
        when(testRequest.getTestSourceDirectory()).thenReturn(new File("."));
        when(providerParameters.getTestRequest()).thenReturn(testRequest);
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

    @BeforeClass
    public static void dumpConfiguration() {
        Configuration.load().dump();
    }

    @AfterClass
    public static void purgeConfiguration() {
        final Path tempDirPath = Paths.get(Paths.get("").toAbsolutePath().toString(), SMART_TESTING_WORKING_DIRECTORY_NAME);
        FileSystemOperations.deleteDirectory(tempDirPath, true);
    }

    private boolean iterableContains(Iterable<Class> iterable, Set expectedClasses) {
        List<Class> actualCall = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
        return actualCall.size() == expectedClasses.size() && actualCall.containsAll(expectedClasses);
    }

    private static class ATest {}

    private static class BTest {}
}
