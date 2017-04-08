package org.arquillian.smart.testing.surefire.provider;

import java.util.HashMap;
import java.util.Map;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ProviderParameterParserTest {

    private ProviderParameters providerParameters;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void feedParameters() {
        Map<String, String> providerProperties = new HashMap<>();
        providerProperties.put("classPathUrl.0", "/home/mjobanek/.m2/repository/org/testng/testng/6.11/testng-6.11.jar");
        providerProperties.put("classPathUrl.1", "/home/mjobanek/.m2/repository/junit/junit/4.12/junit-4.12.jar");
        providerProperties.put("classPathUrl.2",
            "/home/john//.m2/repository/org/apache/maven/surefire/surefire-api/2.19.1/surefire-api-2.19.1.jar");

        providerParameters = Mockito.mock(ProviderParameters.class);
        when(providerParameters.getProviderProperties()).thenReturn(providerProperties);
    }

    @Test
    public void parser_should_return_parsed_versions() {
        ProviderParametersParser parametersParser = new ProviderParametersParser(providerParameters);
        softly.assertThat(parametersParser.getSurefireApiVersion()).isEqualTo("2.19.1");
        softly.assertThat(parametersParser.getJunitVersion()).isEqualTo("4.12");
        softly.assertThat(parametersParser.getTestNgVersion()).isEqualTo("6.11");
    }
}
