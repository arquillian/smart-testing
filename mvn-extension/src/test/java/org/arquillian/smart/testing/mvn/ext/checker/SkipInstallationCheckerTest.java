package org.arquillian.smart.testing.mvn.ext.checker;

import net.jcip.annotations.NotThreadSafe;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.arquillian.smart.testing.mvn.ext.custom.assertions.MavenExtensionSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.arquillian.smart.testing.mvn.ext.checker.SkipInstallationChecker.NO_GOAL_REASON;
import static org.arquillian.smart.testing.mvn.ext.checker.SkipInstallationChecker.NO_TEST_GOAL_REASON;
import static org.arquillian.smart.testing.mvn.ext.checker.SkipInstallationChecker.SPECIFIC_CLASSES_REASON;
import static org.arquillian.smart.testing.mvn.ext.checker.SkipInstallationChecker.TEST_SKIPPED_REASON;

@Category(NotThreadSafe.class)
public class SkipInstallationCheckerTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final MavenExtensionSoftAssertions softly = new MavenExtensionSoftAssertions();

    private MavenSession setUpMavenSession(List<String> goals, String defaultGoal) {
        Build build = Mockito.mock(Build.class);
        Mockito.when(build.getDefaultGoal()).thenReturn(defaultGoal);

        MavenProject topLevelProject = Mockito.mock(MavenProject.class);
        Mockito.when(topLevelProject.getBuild()).thenReturn(build);

        MavenSession mavenSession = Mockito.mock(MavenSession.class);
        Mockito.when(mavenSession.getGoals()).thenReturn(goals);
        Mockito.when(mavenSession.getTopLevelProject()).thenReturn(topLevelProject);

        return mavenSession;
    }

    @Test
    public void checker_should_return_true_when_validate_is_set_regardless_default_goal() {
        // given
        MavenSession mavenSession = setUpMavenSession(Collections.singletonList("clean"), "clean package");

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker)
            .isSkipped(true)
            .forReason(NO_TEST_GOAL_REASON);
    }

    @Test
    public void checker_should_return_false_when_package_in_default_goal_is_set_set() {
        // given
        MavenSession mavenSession = setUpMavenSession(Collections.emptyList(), "clean package");

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker).isSkipped(false);
    }

    @Test
    public void checker_should_return_true_when_clean_validate_in_default_goal_is_set() {
        // given
        MavenSession mavenSession = setUpMavenSession(Collections.emptyList(), "clean validate");

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker)
            .isSkipped(true)
            .forReason(NO_TEST_GOAL_REASON);
    }

    @Test
    public void checker_should_return_true_when_no_goal_is_set() {
        // given
        MavenSession mavenSession = setUpMavenSession(Collections.emptyList(), null);

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker)
            .isSkipped(true)
            .forReason(NO_GOAL_REASON);
    }

    @Test
    public void checker_should_return_true_when_clean_goal_is_set() {
        // given
        MavenSession mavenSession = setUpMavenSession(Collections.singletonList("clean"), null);

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker)
            .isSkipped(true)
            .forReason(NO_TEST_GOAL_REASON);
    }

    @Test
    public void checker_should_return_false_when_package_goal_and_no_skip_property_is_set() {
        // given
        MavenSession mavenSession = setUpMavenSession(Arrays.asList("clean", "package"), null);

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker).isSkipped(false);
    }

    @Test
    public void checker_should_return_false_when_plugin_goal_set_set() {
        // given
        MavenSession mavenSession = setUpMavenSession(
            Arrays.asList("clean", "org.apache.maven.plugins:maven-surefire-plugin:2.20.1:test"), null);

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker).isSkipped(false);
    }

    @Test
    public void checker_should_return_true_when_single_test_is_set() {
        // given
        MavenSession mavenSession = getMavenSessionForProperty("test", "MyCoolTest");

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker)
            .isSkipped(true)
            .forReason(SPECIFIC_CLASSES_REASON);
    }

    @Test
    public void checker_should_return_true_when_two_tests_are_set() {
        // given
        MavenSession mavenSession = getMavenSessionForProperty("test", "MyCoolTest,MySecondTest");

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker)
            .isSkipped(true)
            .forReason(SPECIFIC_CLASSES_REASON);

    }

    @Test
    public void checker_should_return_false_when_pattern_is_set() {
        // given
        MavenSession mavenSession = getMavenSessionForProperty("test", "MyCool*Test");

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker).isSkipped(false);
    }

    @Test
    public void checker_should_return_false_when_classes_and_pattern_are_set() {
        // given
        MavenSession mavenSession = getMavenSessionForProperty("test", "FirstTest,MyCool*Test,LastTest");

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker).isSkipped(false);
    }

    @Test
    public void checker_should_return_true_when_skip_tests_system_property_set() {
        // given
        MavenSession mavenSession = getMavenSessionForProperty("skipTests", "true");

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker)
            .isSkipped(true)
            .forReason(TEST_SKIPPED_REASON);
    }

    @Test
    public void checker_should_return_true_when_maven_test_skip_system_property_set() {
        // given
        MavenSession mavenSession = getMavenSessionForProperty("maven.test.skip", "true");

        // when
        SkipInstallationChecker skipInstallationChecker = new SkipInstallationChecker(mavenSession);

        // then
        softly.assertThat(skipInstallationChecker)
            .isSkipped(true)
            .forReason(TEST_SKIPPED_REASON);
    }

    private MavenSession getMavenSessionForProperty(String property, String value) {
        System.setProperty(property, value);
        return setUpMavenSession(Arrays.asList("clean", "package"), null);
    }
}
