package org.arquillian.smart.testing.mvn.ext.checker;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.mvn.ext.dependencies.ExtensionVersion;

import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING_DISABLE;
import static org.arquillian.smart.testing.mvn.ext.checker.SkipModuleChecker.MAVEN_TEST_SKIP;
import static org.arquillian.smart.testing.mvn.ext.checker.SkipModuleChecker.SKIP_TESTS;

public class SkipInstallationChecker {

    private static final List<String> EXPECTED_GOALS = Arrays.asList(
        "test", "prepare-package", "package", "pre-integration-test", "integration-test",
        "post-integration-test", "verify", "install", "deploy", "pre-site", "site", "post-site", "site-deploy");

    static final String NO_GOAL_REASON = "No goals have been specified for the build.";
    static final String NO_TEST_GOAL_REASON =
        "None of the goals specified for the build will invoke the tests. Any of the following goals are expected: ";
    static final String TEST_SKIPPED_REASON = "Test Execution has been skipped.";
    static final String SPECIFIC_CLASSES_REASON = "Specific Test Class execution is set.";

    private final Pattern TEST_CLASS_PATTERN = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

    private MavenSession session;
    private MavenProject mavenProject;
    private String reason;

    public SkipInstallationChecker(MavenSession session) {
        this.session = session;
    }

    public SkipInstallationChecker(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    public boolean shouldSkip() {
        List<String> goals = session.getGoals();
        String defaultGoal = session.getTopLevelProject().getBuild().getDefaultGoal();
        if (goals.isEmpty() && (defaultGoal == null || defaultGoal.isEmpty())) {
            reason = NO_GOAL_REASON;

        } else {
            if (goals.isEmpty()) {
                goals = Arrays.asList(defaultGoal.split(" "));
            }
            boolean isAnyGoalInvokingTest = goals.stream()
                .anyMatch(goal -> EXPECTED_GOALS.contains(goal) || isPluginGoal(goal));
            if (!isAnyGoalInvokingTest) {
                reason = NO_TEST_GOAL_REASON + EXPECTED_GOALS;

            } else if (isSkipTestExecutionSet()) {
                reason = TEST_SKIPPED_REASON;

            } else if (isSpecificTestClassSet()) {
                reason = SPECIFIC_CLASSES_REASON;
            }
        }

        return reason != null;
    }

    public boolean shouldSkipForConfiguration(Configuration configuration) {
        if (mavenProject == null) {
            mavenProject = session.getTopLevelProject();
        }
        if (configuration.isDisable()) {
            reason = String.format("Disabling Smart Testing %s in %s module. Reason: " + SMART_TESTING_DISABLE + " is set.",
                ExtensionVersion.version().toString(), mavenProject.getArtifactId());
            return true;
        }
        if (!configuration.areStrategiesDefined()) {
            reason = String.format("Smart Testing Extension is installed but no strategies are provided for %s module. It won't influence the way how your tests are executed. "
                + "For details on how to configure it head over to http://bit.ly/st-config", mavenProject.getArtifactId());
            return true;
        }
        return false;
    }

    private boolean isSkipTestExecutionSet() {
        return isSkipTests() || isSkip();
    }

    private boolean isSkipTests() {
        return Boolean.valueOf(System.getProperty(SKIP_TESTS));
    }

    private boolean isSkip() {
        return Boolean.valueOf(System.getProperty(MAVEN_TEST_SKIP));
    }

    private boolean isSpecificTestClassSet() {
        String testClasses = System.getProperty("test");
        return testClasses != null && !containsPattern(testClasses);
    }

    private boolean containsPattern(String testClasses) {
        return Arrays.stream(testClasses.split(","))
            .map(TEST_CLASS_PATTERN::matcher)
            .anyMatch(Matcher::find);
    }

    public String getReason(){
        return reason;
    }

    // TODO needs additional investigation of using plugin goals such as org.apache.maven.plugins:maven-surefire-plugin:2.20.1:test
    private boolean isPluginGoal(String goal) {
        return goal.contains(":");
    }
}
