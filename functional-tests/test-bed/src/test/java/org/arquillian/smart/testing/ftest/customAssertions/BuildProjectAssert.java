package org.arquillian.smart.testing.ftest.customAssertions;

import java.io.File;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.FileAssert;
import org.assertj.core.api.JUnitSoftAssertions;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.junit.Rule;

import static org.arquillian.smart.testing.ftest.customAssertions.CustomAssertions.assertThat;

public class BuildProjectAssert extends AbstractAssert<BuildProjectAssert, BuiltProject> {

    @Rule
    public static final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    BuildProjectAssert(BuiltProject actual) {
        super(actual, BuildProjectAssert.class);
    }

    public BuildProjectAssert hasAllBuiltSubmodulesContainBuildArtifact(String directory) {
        actual.getModules().forEach(subModule -> assertThat(subModule).hasFileIncludedIn(directory));
        return this;
    }

    private BuildProjectAssert hasFileIncludedIn(String directory) {
        final File targetDirectory = actual.getTargetDirectory();
        final FileAssert fileAssert = softly.assertThat(new File(targetDirectory, directory));
        if (isJar(actual)) {
            if (testsWereExecuted(targetDirectory)) {
                fileAssert.exists();
            } else {
                fileAssert.doesNotExist();
            }
        } else {
            hasAllBuiltSubmodulesContainBuildArtifact(directory);
            fileAssert.doesNotExist();
        }

        return this;
    }

    private static boolean isJar(BuiltProject subModule) {
        return subModule.getModel().getPackaging().equals("jar");
    }

    private static boolean testsWereExecuted(File target) {
        return new File(target, "test-classes").exists();
    }
}
