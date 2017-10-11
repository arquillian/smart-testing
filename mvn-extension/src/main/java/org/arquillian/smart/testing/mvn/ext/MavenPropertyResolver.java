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

    private Model model;
    private Plugin surefirePlugin;
    private Plugin failsafePlugin;

    private static final Pattern TEST_CLASS_PATTERN = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

    MavenPropertyResolver(MavenProject mavenProject) {
        this.model = mavenProject.getModel();
        this.surefirePlugin = mavenProject.getPlugin("org.apache.maven.plugins:maven-surefire-plugin");
        this.failsafePlugin = mavenProject.getPlugin("org.apache.maven.plugins:maven-failsafe-plugin");
    }

    MavenPropertyResolver(Model model) {
        this.model = model;
        this.failsafePlugin = model.getBuild()
            .getPlugins()
            .stream()
            .filter(plugin -> plugin.getArtifactId().equals("maven-failsafe-plugin"))
            .collect(Collectors.toList()).get(0);
    }

    static boolean isSkipTestExecutionSet() {
        return isSkipTests() || isSkip();
    }

    static private boolean isSkipTests() {
        return Boolean.valueOf(System.getProperty("skipTests", "false"));
    }

    boolean isSkipITs() {
        return Boolean.valueOf(System.getProperty("skipITs", "false")) || isSkipITsSetInPom();
    }

    static private boolean isSkip() {
        return Boolean.valueOf(System.getProperty("maven.test.skip", "false"));
    }

    boolean isSkipTestsSetInPom() {
        if (surefirePlugin != null && failsafePlugin == null) {
            Xpp3Dom surefirePluginConfiguration = (Xpp3Dom) surefirePlugin.getConfiguration();
            return isSkipTestsSetInPluginConfiguration(surefirePluginConfiguration) ||
                isSkipSetInPluginConfiguration(surefirePluginConfiguration);
        }
        return isSkipSetInProperty() || isSkipTestsSetInProperty();
    }

    private Boolean isSkipITsSetInPom() {
        if (failsafePlugin != null) {
            Xpp3Dom configuration = (Xpp3Dom) failsafePlugin.getConfiguration();
            return isSkipSetInPluginConfiguration(configuration) || isSkipTestsSetInPluginConfiguration(configuration)
                || isSkipITsSetInPluginConfiguration(configuration);
        }
        return isSkipITsSetInProperty();
    }

    private boolean isSkipTestsSetInProperty() {
        String skipTestsProperty = model.getProperties().getProperty("skipTests");
        return Boolean.valueOf(skipTestsProperty);
    }

    private boolean isSkipSetInProperty() {
        String mavenSkipProperty = model.getProperties().getProperty("maven.test.skip");
        return Boolean.valueOf(mavenSkipProperty);
    }

    private boolean isSkipITsSetInProperty() {
        String skipITsProperty = model.getProperties().getProperty("skipITs");
        return Boolean.valueOf(skipITsProperty);
    }

    private boolean isSkipTestsSetInPluginConfiguration(Xpp3Dom pluginConfiguration) {
        Xpp3Dom skipTests = pluginConfiguration.getChild("skipTests");
        if (skipTests == null) {
            return isSkipSetInProperty();
        }
        return "true".equals(skipTests.getValue());
    }

    private boolean isSkipSetInPluginConfiguration(Xpp3Dom pluginConfiguration) {
        Xpp3Dom skip = pluginConfiguration.getChild("skip");
        if (skip == null) {
            return isSkipSetInProperty();
        }
        return "true".equals(skip.getValue());
    }

    private boolean isSkipITsSetInPluginConfiguration(Xpp3Dom pluginConfiguration) {
        Xpp3Dom skipITs = pluginConfiguration.getChild("skipITs");
        if (skipITs == null) {
            return isSkipITsSetInProperty();
        }
        return "true".equals(skipITs.getValue());
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
