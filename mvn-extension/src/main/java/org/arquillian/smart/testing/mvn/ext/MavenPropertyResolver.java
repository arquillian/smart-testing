package org.arquillian.smart.testing.mvn.ext;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

class MavenPropertyResolver {

    private static final Pattern TEST_CLASS_PATTERN = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

    static boolean isSkipTestExecutionSet() {
        return isSkipTests() || isSkip();
    }

    static private boolean isSkipTests() {
        return Boolean.valueOf(System.getProperty("skipTests", "false"));
    }

    static boolean isSkipITs(Model model) {
        return Boolean.valueOf(System.getProperty("skipITs", "false")) || isSkipITsSetInPom(model);
    }

    static private boolean isSkip() {
        return Boolean.valueOf(System.getProperty("maven.test.skip", "false"));
    }

    static boolean isSkipTestsSetInPom(MavenProject mavenProject) {
        String skipTestsProperty = mavenProject.getProperties().getProperty("skipTests");
        Plugin surefirePlugin = mavenProject.getPlugin("org.apache.maven.plugins:maven-surefire-plugin");
        if (surefirePlugin != null) {
            Xpp3Dom surefirePluginConfiguration = (Xpp3Dom) surefirePlugin.getConfiguration();
            return Boolean.valueOf(skipTestsProperty) || isSkipTestsPropertyConfigured(surefirePluginConfiguration)
                || isSkipPropertyConfigured(surefirePluginConfiguration);
        }
        return Boolean.valueOf(skipTestsProperty);
    }

    private static Boolean isSkipITsSetInPom(Model model) {
        String skipITsProperty = model.getProperties().getProperty("skipITs");
        Plugin failsafePlugin = model.getBuild()
            .getPlugins()
            .stream()
            .filter(plugin -> plugin.getArtifactId().equals("maven-failsafe-plugin"))
            .collect(Collectors.toList()).get(0);

        if (failsafePlugin != null) {
            Xpp3Dom configuration = (Xpp3Dom) failsafePlugin.getConfiguration();
            return Boolean.valueOf(skipITsProperty) || isSkipPropertyConfigured(configuration)
                || isSkipTestsPropertyConfigured(configuration) || isSkipITsPropertyConfigured(configuration);
        }
        return Boolean.valueOf(skipITsProperty);
    }

    private static boolean isSkipTestsPropertyConfigured(Xpp3Dom pluginConfiguration) {
        Xpp3Dom skipTests = pluginConfiguration.getChild("skipTests");
        return skipTests != null && "true".equals(skipTests.getValue());
    }

    private static boolean isSkipPropertyConfigured(Xpp3Dom pluginConfiguration) {
        Xpp3Dom skip = pluginConfiguration.getChild("skip");
        return skip != null && "true".equals(skip.getValue());
    }

    private static boolean isSkipITsPropertyConfigured(Xpp3Dom pluginConfiguration) {
        Xpp3Dom skipITs = pluginConfiguration.getChild("skipITs");
        return skipITs != null && "true".equals(skipITs.getValue());
    }

    static boolean isSpecificTestClassSet() {
        String testClasses = System.getProperty("test");
        return testClasses != null && !containsPattern(testClasses);
    }

    private static boolean containsPattern(String testClasses) {
        return Arrays.stream(testClasses.split(","))
            .map(TEST_CLASS_PATTERN::matcher)
            .anyMatch(Matcher::find);
    }
}
