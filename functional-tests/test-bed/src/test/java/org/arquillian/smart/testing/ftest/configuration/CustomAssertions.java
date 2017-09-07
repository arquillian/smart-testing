package org.arquillian.smart.testing.ftest.configuration;

import java.io.File;
import org.assertj.core.api.FileAssert;
import org.assertj.core.api.JUnitSoftAssertions;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.junit.Rule;

class CustomAssertions {

    @Rule
    public static final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    static void assertThatAllBuiltSubmodulesHaveReportsIncluded(BuiltProject module, String reportDir) {
        module.getModules().forEach(subModule -> assertThatReportFileIsIncludedIn(subModule, reportDir));
    }

    private static void assertThatReportFileIsIncludedIn(BuiltProject subModule, String reportDir) {
        final File targetDirectory = subModule.getTargetDirectory();
        final FileAssert fileAssert = softly.assertThat(new File(targetDirectory, reportDir));
        if (isJar(subModule)) {
            if (testsWereExecuted(targetDirectory)) {
                fileAssert.exists();
            } else {
                fileAssert.doesNotExist();
            }
        } else {
            assertThatAllBuiltSubmodulesHaveReportsIncluded(subModule, reportDir);
            fileAssert.doesNotExist();
        }
    }

    private static boolean isJar(BuiltProject subModule) {
        return subModule.getModel().getPackaging().equals("jar");
    }

    private static boolean testsWereExecuted(File target) {
        return new File(target, "test-classes").exists();
    }
}
