package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class LoaderVersionExtractorSurefireJUnit5Test {

    private final String surefirejunitVersion;

    public LoaderVersionExtractorSurefireJUnit5Test(String junitSurefireVersion) {
        this.surefirejunitVersion = junitSurefireVersion;
    }

    @Parameterized.Parameters
    public static Collection<String> data() {
        return Arrays.asList("1.0.0");
    }

    @Test
    public void test_should_load_surefire_junit_version() throws MalformedURLException {
        // given
        File junitFile = Maven
            .resolver()
            .resolve("org.junit.platform:junit-platform-surefire-provider:" + surefirejunitVersion)
            .withoutTransitivity()
            .asSingleFile();

        URL[] junitUrl = {junitFile.toURI().toURL()};
        URLClassLoader urlClassLoader = new URLClassLoader(junitUrl, null);

        // when
        String junitVersion =
            LoaderVersionExtractor.getVersionFromClassLoader(LoaderVersionExtractor.LIBRARY_SUREFIRE_JUNIT_5, urlClassLoader);

        // then
        assertThat(junitVersion).isEqualTo(junitVersion);
    }

}
