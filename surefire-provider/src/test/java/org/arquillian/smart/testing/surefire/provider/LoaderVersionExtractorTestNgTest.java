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
public class LoaderVersionExtractorTestNgTest {

    private final String testNgVersion;

    public LoaderVersionExtractorTestNgTest(String testNgVersion) {
        this.testNgVersion = testNgVersion;
    }

    @Parameterized.Parameters
    public static Collection<String> data() {
        return Arrays.asList("6.8.8", "6.9.4", "6.9.10", "6.10", "6.11");
    }

    @Test
    public void test_should_load_testng_version() throws MalformedURLException {
        // given
        File junitFile = Maven
            .resolver()
            .resolve("org.testng:testng:" + testNgVersion)
            .withoutTransitivity()
            .asSingleFile();

        URL[] junitUrl = {junitFile.toURI().toURL()};
        URLClassLoader urlClassLoader = new URLClassLoader(junitUrl);

        // when
        String junitVersion =
            LoaderVersionExtractor.getVersionFromClassLoader(LoaderVersionExtractor.LIBRARY_TEST_NG, urlClassLoader);

        // then
        assertThat(junitVersion).isEqualTo(testNgVersion);
    }
}
