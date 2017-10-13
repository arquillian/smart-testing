package org.arquillian.smart.testing.mvn.ext;

import java.util.Arrays;
import java.util.List;

import static org.arquillian.smart.testing.mvn.ext.MavenPropertyResolver.isSkipTestExecutionSet;
import static org.arquillian.smart.testing.mvn.ext.MavenPropertyResolver.isSpecificTestClassSet;

class SkipInstallationChecker {

    private static final List<String> EXPECTED_GOALS = Arrays.asList(
        new String[] {"test", "prepare-package", "package", "pre-integration-test", "integration-test",
            "post-integration-test", "verify", "install", "deploy", "pre-site", "site", "post-site", "site-deploy"});
    private final List<String> goals;
    private String reason;

    SkipInstallationChecker(List<String> goals) {

        this.goals = goals;
    }

    boolean shouldSkip() {
        if (goals.size() == 0) {
            reason = "No goals have been specified for the build.";

        } else {
            boolean isAnyGoalInvokingTest = goals.stream()
                .anyMatch(goal -> EXPECTED_GOALS.contains(goal) || isGoalSpecification(goal));
            if (!isAnyGoalInvokingTest) {
                reason =
                    "None of the goals specified for the build will invoke the tests. Any of the following goals are expected: "
                        + EXPECTED_GOALS;

            } else if (isSkipTestExecutionSet()) {
                reason = "Test Execution has been skipped.";

            } else if (isSpecificTestClassSet()) {
                reason = "Single Test Class execution is set.";
            }
        }

        return reason != null;
    }

    String getReason(){
        return reason;
    }

    private boolean isGoalSpecification(String goal) {
        return goal.indexOf(':') >= 0;
    }
}
