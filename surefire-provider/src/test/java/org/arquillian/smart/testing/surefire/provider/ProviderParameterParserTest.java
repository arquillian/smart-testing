package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.testset.DirectoryScannerParameters;
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
        DirectoryScannerParameters directoryScannerParameters = new DirectoryScannerParameters(
            new File(""),
            Arrays.asList(new String[] {"**/*Test.java", "**/*TestCase.java", "**/Test*.java"}),
            Arrays.asList(new String[] {"**/*$*", "**/*Failing.java"}),
            new ArrayList<>(),
            false,
            "filesystem");

        providerParameters = Mockito.mock(ProviderParameters.class);
        when(providerParameters.getDirectoryScannerParameters()).thenReturn(directoryScannerParameters);
    }

    @Test
    public void parser_should_return_parsed_includes_and_excludes() {
        ProviderParametersParser parametersParser = new ProviderParametersParser(providerParameters);
        softly.assertThat(parametersParser.getIncludes())
            .containsExactly("**/*Test.java", "**/*TestCase.java", "**/Test*.java");
        softly.assertThat(parametersParser.getExcludes()).containsExactly("**/*$*", "**/*Failing.java");
    }
}
