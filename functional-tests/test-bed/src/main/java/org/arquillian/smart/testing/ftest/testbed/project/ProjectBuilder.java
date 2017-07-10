package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;

import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.FAILURE;
import static org.arquillian.smart.testing.ftest.testbed.testresults.Status.PASSED;
import static org.arquillian.smart.testing.ftest.testbed.testresults.SurefireReportReader.loadTestResults;

class ProjectBuilder {

    private static final String TEST_REPORT_PREFIX = "TEST-";

    private final Path root;
    private final Properties envVariables = new Properties();

    ProjectBuilder(Path root) {
        this.root = root;
    }

    List<TestResult> build(String... goals) {
        final BuiltProject build = EmbeddedMaven.forProject(root.toAbsolutePath().toString() + "/pom.xml")
                    .setGoals(goals)
                    .setQuiet()
                    .skipTests(false)
                    .setProperties(envVariables)
                    .ignoreFailure()
                .build();

        if (build.getMavenBuildExitCode() != 0) {
            System.out.println(build.getMavenLog());
            throw new IllegalStateException("Maven build has failed, see logs for details");
        }

        return accumulatedTestResults();
    }

    ProjectBuilder withEnvVariables(String ... envVariablesPairs) {
        if (envVariablesPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Expecting even amount of variable name - value pairs to be passed. Got "
                + envVariablesPairs.length
                + " entries. "
                + envVariablesPairs);
        }

        for (int i = 0; i < envVariablesPairs.length; i += 2) {
            this.envVariables.put(envVariablesPairs[i], envVariablesPairs[i + 1]);
        }

        return this;
    }

    private List<TestResult> accumulatedTestResults() {
        try {
            return Files.walk(root)
                .filter(path -> path.getFileName().toString().startsWith(TEST_REPORT_PREFIX))
                .map(path -> {
                        try {
                            final Set<TestResult> testResults = loadTestResults(new FileInputStream(path.toFile()));
                            return testResults.stream()
                                .reduce(new TestResult("", "*", PASSED),
                                    (previous, current) -> new TestResult(current.getClassName(), "*",
                                        (previous.isFailing() || current.isFailing()) ? FAILURE : PASSED));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                )
                .distinct()
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed extracting test results", e);
        }
    }
}
