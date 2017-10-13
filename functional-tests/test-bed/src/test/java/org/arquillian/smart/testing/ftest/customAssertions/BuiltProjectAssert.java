package org.arquillian.smart.testing.ftest.customAssertions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.FileAssert;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;

import static org.arquillian.smart.testing.hub.storage.local.AfterExecutionLocalStorage.REPORTING_SUBDIRECTORY;
import static org.arquillian.smart.testing.hub.storage.local.AfterExecutionLocalStorage.SMART_TESTING_TARGET_DIRECTORY_NAME;
import static org.assertj.core.api.Assertions.contentOf;

public class BuiltProjectAssert extends AbstractAssert<BuiltProjectAssert, BuiltProject> {

    BuiltProjectAssert(BuiltProject actual) {
        super(actual, BuiltProjectAssert.class);
    }

    public static BuiltProjectAssert assertThat(BuiltProject module) {
        return new BuiltProjectAssert(module);
    }

    /**
     * Will assert that all built sub-modules (with packaging type other than pom) contain effective pom.xml with
     * smart testing extension configured.
     *
     * @param effectivePom
     *     effectivePom file to verify
     **/
    public BuiltProjectAssert allBuiltSubModulesContainEffectivePom(String effectivePom) {
        actual.getModules().forEach(subModule -> assertThat(subModule).allBuiltSubModulesContainEffectivePom(effectivePom));
        containsEffectivePom(effectivePom);
        return this;
    }

    /**
     * Will assert that all built sub-modules with test executions contain report.
     *
     * @param report
     *     report file to verify
     **/
    public BuiltProjectAssert allBuiltSubModulesWithTestExecutionsContainReport(String report) {
        actual.getModules().forEach(subModule -> assertThat(subModule).allBuiltSubModulesWithTestExecutionsContainReport(report));
        hasReportFile(report);
        return this;
    }

    private void containsEffectivePom(String effectivePom) {
        final String smartTestingExtension = "org.arquillian.smart.testing";
        final File targetDirectory = actual.getTargetDirectory();
        Path reportPath = Paths.get(targetDirectory.toString(), SMART_TESTING_TARGET_DIRECTORY_NAME, REPORTING_SUBDIRECTORY);
        if (targetDirectory.exists() && !actual.getModel().getPackaging().equals("pom")) {
            final File pomFile = new File(reportPath.toString(), effectivePom);
            FileAssert fileAssert = new FileAssert(pomFile);
            fileAssert.exists();
            Assertions.assertThat(contentOf(pomFile)).contains(smartTestingExtension);
        }
    }

    private void hasReportFile(String report) {
        final File targetDirectory = actual.getTargetDirectory();
        Path reportPath = Paths.get(targetDirectory.toString(), SMART_TESTING_TARGET_DIRECTORY_NAME, REPORTING_SUBDIRECTORY);
        final FileAssert fileAssert = new FileAssert(new File(reportPath.toString(), report));
        if (isJar(actual)) {
            if (hasCompiledTests(targetDirectory)) {
                fileAssert.exists();
            } else {
                fileAssert.doesNotExist();
            }
        } else {
            fileAssert.doesNotExist();
        }
    }

    private static boolean isJar(BuiltProject subModule) {
        return subModule.getModel().getPackaging().equals("jar");
    }

    private static boolean hasCompiledTests(File target) {
        return new File(target, "test-classes").exists();
    }
}
