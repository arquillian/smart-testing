package org.arquillian.smart.testing.ftest.customAssertions;

import java.io.File;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.FileAssert;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;

public class BuiltProjectAssert extends AbstractAssert<BuiltProjectAssert, BuiltProject> {

    BuiltProjectAssert(BuiltProject actual) {
        super(actual, BuiltProjectAssert.class);
    }

    public static BuiltProjectAssert assertThat(BuiltProject module) {
        return new BuiltProjectAssert(module);
    }

    public BuiltProjectAssert allBuiltSubModulesContainEffectivePom(String effectivePom) {
        actual.getModules().forEach(subModule -> assertThat(subModule).containsEffectivePom(effectivePom));
        return this;
    }

    /**
     * Will assert that all built sub-modules with test executions contain report.
     *
     * @param report
     *     report file to verify
     * */
    public BuiltProjectAssert allBuiltSubModulesWithTestExecutionsContainReport(String report) {
        actual.getModules().forEach(subModule -> assertThat(subModule).hasReportFile(report));
        return this;
    }

    private BuiltProjectAssert containsEffectivePom(String effectivePom) {
        if (actual.getTargetDirectory().exists()) {
            FileAssert fileAssert = new FileAssert(new File(actual.getTargetDirectory(), effectivePom));
            fileAssert.exists();
        }
        return this;
    }

    private BuiltProjectAssert hasReportFile(String report) {
        final File targetDirectory = actual.getTargetDirectory();
        final FileAssert fileAssert = new FileAssert(new File(targetDirectory, report));
        if (isJar(actual)) {
            if (testsWereExecuted(targetDirectory)) {
                fileAssert.exists();
            } else {
                fileAssert.doesNotExist();
            }
        } else {
            allBuiltSubModulesWithTestExecutionsContainReport(report);
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
