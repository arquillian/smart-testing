package org.arquillian.smart.testing.surefire.provider;

import java.util.HashMap;
import java.util.Map;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.arquillian.smart.testing.surefire.provider.info.TestNgProviderInfo;
import org.junit.Test;
import org.mockito.Mockito;

import static org.apache.maven.surefire.booter.ProviderParameterNames.PARALLEL_PROP;
import static org.apache.maven.surefire.booter.ProviderParameterNames.THREADCOUNT_PROP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TestNgParametersTest {

    @Test
    public void should_remove_zero_thread_count() {
        // given
        ProviderParameters providerParameters = prepareProviderParams("0");

        // when
        TestNgProviderInfo testNgProviderInfo = new TestNgProviderInfo();
        ProviderParameters convertedProviderParams = testNgProviderInfo.convertProviderParameters(providerParameters);

        // then
        assertThat(convertedProviderParams.getProviderProperties()).hasSize(1).doesNotContainKeys(THREADCOUNT_PROP);
    }

    @Test
    public void should_not_remove_positive_thread_count() {
        // given
        ProviderParameters providerParameters = prepareProviderParams("1");

        // when
        TestNgProviderInfo testNgProviderInfo = new TestNgProviderInfo();
        ProviderParameters convertedProviderParams = testNgProviderInfo.convertProviderParameters(providerParameters);

        // then
        assertThat(convertedProviderParams.getProviderProperties()).hasSize(2).containsKeys(THREADCOUNT_PROP);
    }


    private ProviderParameters prepareProviderParams(String threadCount){
        Map<String, String> providerProperties = new HashMap<>();
        providerProperties.put(PARALLEL_PROP, "none");
        providerProperties.put(THREADCOUNT_PROP, threadCount);

        ProviderParameters providerParameters = Mockito.mock(ProviderParameters.class);
        when(providerParameters.getProviderProperties()).thenReturn(providerProperties);

        return providerParameters;
    }
}
