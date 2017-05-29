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
        providerProperties.put("includes1", "**/*Test.java");
        providerProperties.put("includes2", "**/*TestCase.java");
        providerProperties.put("includes0", "**/Test*.java");
        providerProperties.put("excludes0", "**/*$*");
        providerProperties.put("excludes1", "**/*Failing.java");

        providerParameters = Mockito.mock(ProviderParameters.class);
        when(providerParameters.getProviderProperties()).thenReturn(providerProperties);
    }

    @Test
    public void parser_should_return_parsed_includes_and_excludes() {
        ProviderParametersParser parametersParser = new ProviderParametersParser(providerParameters);
        softly.assertThat(parametersParser.getIncludes())
            .containsExactly("**/Test*.java", "**/*Test.java", "**/*TestCase.java");
        softly.assertThat(parametersParser.getExcludes()).containsExactly("**/*$*", "**/*Failing.java");
    }
}
