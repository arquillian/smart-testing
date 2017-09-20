package org.arquillian.smart.testing.ftest.customAssertions;

import java.io.File;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.FileAssert;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;

public class BuildProjectAssert extends AbstractAssert<BuildProjectAssert, BuiltProject> {

    BuildProjectAssert(BuiltProject actual) {
        super(actual, BuildProjectAssert.class);
    }

    public static BuildProjectAssert assertThat(BuiltProject module) {
        return new BuildProjectAssert(module);
    }

    public BuildProjectAssert hasAllBuiltSubModulesContainEffectivePom(String effectivePom) {
        actual.getModules().forEach(subModule -> assertThat(subModule).containsEffectivePom(effectivePom));
        return this;
    }

    public BuildProjectAssert hasAllBuiltSubModulesContainReport(String report) {
        actual.getModules().forEach(subModule -> assertThat(subModule).hasReportFile(report));
        return this;
    }

    private BuildProjectAssert containsEffectivePom(String effectivePom) {
        if (actual.getTargetDirectory().exists()) {
            FileAssert fileAssert = new FileAssert(new File(actual.getTargetDirectory(), effectivePom));
            fileAssert.exists();
        }
        return this;
    }

    private BuildProjectAssert hasReportFile(String report) {
        final File targetDirectory = actual.getTargetDirectory();
        final FileAssert fileAssert = new FileAssert(new File(targetDirectory, report));
        if (isJar(actual)) {
            if (testsWereExecuted(targetDirectory)) {
                fileAssert.exists();
            } else {
                fileAssert.doesNotExist();
            }
        } else {
            hasAllBuiltSubModulesContainReport(report);
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
