package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import net.jcip.annotations.NotThreadSafe;
import shaded.org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
@Category(NotThreadSafe.class)
public class LoaderVersionExtractorJUnitTest {

    private final String junitVersion;

    public LoaderVersionExtractorJUnitTest(String junitVersion) {
        this.junitVersion = junitVersion;
    }

    @Parameterized.Parameters
    public static Collection<String> data() {
        return Arrays.asList("4.9", "4.10", "4.11", "4.12");
    }

    @Test
    public void test_should_load_junit_version() throws MalformedURLException {
        // given
        File junitFile = Maven
            .resolver()
            .resolve("junit:junit:" + junitVersion)
            .withoutTransitivity()
            .asSingleFile();

        URL[] junitUrl = {junitFile.toURI().toURL()};
        URLClassLoader urlClassLoader = new URLClassLoader(junitUrl, null);

        // when
        String junitVersion =
            LoaderVersionExtractor.getVersionFromClassLoader(LoaderVersionExtractor.LIBRARY_JUNIT, urlClassLoader);

        // then
        assertThat(junitVersion).isEqualTo(junitVersion);
    }
}
