package org.arquillian.smart.testing.mvn.ext;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.arquillian.smart.testing.mvn.ext.ApplicablePlugins.FAILSAFE;
import static org.arquillian.smart.testing.mvn.ext.ApplicablePlugins.SUREFIRE;

class MavenPropertyResolver {

    private final Model model;
    private final Plugin surefirePlugin;
    private final Plugin failsafePlugin;

    private static final String SKIP_TESTS = "skipTests";
    private static final String SKIP_ITs = "skipITs";
    private static final String SKIP = "skip";
    private static final String MAVEN_TEST_SKIP = "maven.test.skip";

    private static final Pattern TEST_CLASS_PATTERN = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

    MavenPropertyResolver(Model model) {
        this.model = model;
        this.surefirePlugin = getPlugin(SUREFIRE);
        this.failsafePlugin = getPlugin(FAILSAFE);
    }

    static boolean isSkipTestExecutionSet() {
        return isSkipTests() || isSkip();
    }

    boolean isSkipITs() {
        return Boolean.valueOf(System.getProperty(SKIP_ITs)) || isSkipITsSetInPom();
    }

    boolean isSkipTestsSetInPom() {
        if (surefirePlugin != null && failsafePlugin == null) {
            Xpp3Dom surefirePluginConfiguration = (Xpp3Dom) surefirePlugin.getConfiguration();
            if (surefirePluginConfiguration != null) {
                return isPropertySetInPluginConfiguration(surefirePluginConfiguration, SKIP) ||
                    isPropertySetInPluginConfiguration(surefirePluginConfiguration, SKIP_TESTS);
            }
        }
        return isPropertyInPom(MAVEN_TEST_SKIP) || isPropertyInPom(SKIP_TESTS);
    }

    static boolean isSpecificTestClassSet() {
        String testClasses = System.getProperty("test");
        return testClasses != null && !containsPattern(testClasses);
    }

    private static boolean isSkipTests() {
        return Boolean.valueOf(System.getProperty(SKIP_TESTS));
    }

    private static boolean isSkip() {
        return Boolean.valueOf(System.getProperty(MAVEN_TEST_SKIP));
    }

    private Boolean isSkipITsSetInPom() {
        if (failsafePlugin != null) {
            Xpp3Dom configuration = (Xpp3Dom) failsafePlugin.getConfiguration();
            if (configuration != null) {
                return isPropertySetInPluginConfiguration(configuration, SKIP)
                    || isPropertySetInPluginConfiguration(configuration, SKIP_TESTS)
                    || isPropertySetInPluginConfiguration(configuration, SKIP_ITs);
            }
        }
        return isPropertyInPom(SKIP_ITs);
    }

    private boolean isPropertySetInPluginConfiguration(Xpp3Dom pluginConfiguration, String property) {
        Xpp3Dom propertyKey = pluginConfiguration.getChild(property);
        if (propertyKey == null) {
            if (property.equals(SKIP)) {
                return isPropertyInPom(MAVEN_TEST_SKIP);
            }
            return isPropertyInPom(property);
        }
        return Boolean.valueOf(propertyKey.getValue());
    }

    private boolean isPropertyInPom(String property) {
        String key = model.getProperties().getProperty(property);
        return Boolean.valueOf(key);
    }

    private Plugin getPlugin(ApplicablePlugins applicablePlugin) {
        return model.getBuild()
            .getPlugins()
            .stream()
            .filter(plugin -> plugin.getArtifactId().equals(applicablePlugin.getArtifactId()))
            .findFirst()
            .orElse(null);
    }

    private static boolean containsPattern(String testClasses) {
        return Arrays.stream(testClasses.split(","))
            .map(TEST_CLASS_PATTERN::matcher)
            .anyMatch(Matcher::find);
    }
}
