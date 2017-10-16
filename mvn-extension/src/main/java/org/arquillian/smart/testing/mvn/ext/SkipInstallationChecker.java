package org.arquillian.smart.testing.mvn.ext;

import java.util.Arrays;
import java.util.List;
import org.apache.maven.execution.MavenSession;

import static org.arquillian.smart.testing.mvn.ext.MavenPropertyResolver.isSkipTestExecutionSet;
import static org.arquillian.smart.testing.mvn.ext.MavenPropertyResolver.isSpecificTestClassSet;

class SkipInstallationChecker {

    private static final List<String> EXPECTED_GOALS = Arrays.asList(
        new String[] {"test", "prepare-package", "package", "pre-integration-test", "integration-test",
            "post-integration-test", "verify", "install", "deploy", "pre-site", "site", "post-site", "site-deploy"});

    static final String NO_GOAL_REASON = "No goals have been specified for the build.";
    static final String NO_TEST_GOAL_REASON =
        "None of the goals specified for the build will invoke the tests. Any of the following goals are expected: ";
    static final String TEST_SKIPPED_REASON = "Test Execution has been skipped.";
    static final String SPECIFIC_CLASSES_REASON = "Specific Test Class execution is set.";

    private final MavenSession session;
    private String reason;

    SkipInstallationChecker(MavenSession session) {
        this.session = session;
    }

    boolean shouldSkip() {
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

    String getReason(){
        return reason;
    }

    // TODO needs additional investigation of using plugin goals such as org.apache.maven.plugins:maven-surefire-plugin:2.20.1:test
    private boolean isPluginGoal(String goal) {
        return goal.contains(":");
    }
}
