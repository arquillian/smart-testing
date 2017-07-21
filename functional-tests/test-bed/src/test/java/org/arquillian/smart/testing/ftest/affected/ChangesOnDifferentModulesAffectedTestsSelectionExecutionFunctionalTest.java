package org.arquillian.smart.testing.ftest.affected;

import java.util.List;
import org.arquillian.smart.testing.ftest.TestBedTemplate;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangesOnDifferentModulesAffectedTestsSelectionExecutionFunctionalTest extends TestBedTemplate {

    @Test
    public void should_detect_changes_on_maven_modules_and_execute_test() {
        // given
        project.configureSmartTesting()
            .executionOrder(AFFECTED)
            .inMode(SELECTING)
            .enable();

        final List<TestResult> expectedTestResults = project.applyAsCommits("Adds class in one module to affect test in another");

        // when
        final List<TestResult> actualTestResults = project
            .buildOptions()
            .withSystemProperties("git.commit", "HEAD", "git.previous.commit", "HEAD~")
            .configure()
            .build();

        // then
        assertThat(actualTestResults).hasSameSizeAs(expectedTestResults).containsAll(expectedTestResults);
    }

}
