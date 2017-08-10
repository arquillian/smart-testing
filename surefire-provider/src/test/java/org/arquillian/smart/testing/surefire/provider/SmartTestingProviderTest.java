package org.arquillian.smart.testing.surefire.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.testset.DirectoryScannerParameters;
import org.apache.maven.surefire.util.TestsToRun;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SmartTestingProviderTest {

    private Set<Class<?>> expectedClassesToRun = new LinkedHashSet<>(asList(ATest.class, BTest.class));
    private ProviderParameters providerParameters;
    private SurefireProviderFactory providerFactory;

    private SurefireProvider surefireProvider;

    @Before
    public void setupMocks() {
        DirectoryScannerParameters directoryScannerParameters = mock(DirectoryScannerParameters.class);
        when(directoryScannerParameters.getIncludes()).thenReturn(new ArrayList<>());
        when(directoryScannerParameters.getExcludes()).thenReturn(new ArrayList<>());

        providerParameters = mock(ProviderParameters.class);
        when(providerParameters.getDirectoryScannerParameters()).thenReturn(directoryScannerParameters);

        surefireProvider = mock(SurefireProvider.class);
        when(surefireProvider.getSuites()).thenReturn(new TestsToRun(expectedClassesToRun));

        providerFactory = mock(SurefireProviderFactory.class);
        when(providerFactory.createInstance()).thenReturn(surefireProvider);
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

    private boolean iterableContains(Iterable<Class> iterable, Set expectedClasses) {
        List<Class> actualCall = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
        return actualCall.size() == expectedClasses.size() && actualCall.containsAll(expectedClasses);
    }

    private static class ATest {}

    private static class BTest {}
}
