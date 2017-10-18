package org.arquillian.smart.testing.mvn.ext;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.arquillian.smart.testing.mvn.ext.ApplicablePlugins.FAILSAFE;
import static org.arquillian.smart.testing.mvn.ext.ApplicablePlugins.SUREFIRE;

class MavenPropertyResolver {

    private final Model model;
    private final Plugin surefirePlugin;
    private final Plugin failsafePlugin;

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
        return Boolean.valueOf(System.getProperty("skipITs", "false")) || isSkipITsSetInPom();
    }

    boolean isSkipTestsSetInPom() {
        if (surefirePlugin != null && failsafePlugin == null) {
            Xpp3Dom surefirePluginConfiguration = (Xpp3Dom) surefirePlugin.getConfiguration();
            return isPropertySetInPluginConfiguration(surefirePluginConfiguration, "skip") ||
                isPropertySetInPluginConfiguration(surefirePluginConfiguration, "skipTests");
        }
        return isPropertyInPom("maven.test.skip") || isPropertyInPom("skipTests");
    }

    static boolean isSpecificTestClassSet() {
        String testClasses = System.getProperty("test");
        return testClasses != null && !containsPattern(testClasses);
    }

    static private boolean isSkipTests() {
        return Boolean.valueOf(System.getProperty("skipTests", "false"));
    }

    static private boolean isSkip() {
        return Boolean.valueOf(System.getProperty("maven.test.skip", "false"));
    }

    private Boolean isSkipITsSetInPom() {
        if (failsafePlugin != null) {
            Xpp3Dom configuration = (Xpp3Dom) failsafePlugin.getConfiguration();
            return isPropertySetInPluginConfiguration(configuration, "skip")
                || isPropertySetInPluginConfiguration(configuration, "skipTests")
                || isPropertySetInPluginConfiguration(configuration, "skipITs");
        }
        return isPropertyInPom("skipITs");
    }

    private boolean isPropertySetInPluginConfiguration(Xpp3Dom pluginConfiguration, String property) {
        Xpp3Dom propertyKey = pluginConfiguration.getChild(property);
        if (propertyKey == null) {
            if (property.equals("skip")) {
                return isPropertyInPom("maven.test.skip");
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
        List<Plugin> pluginList = model.getBuild()
            .getPlugins()
            .stream()
            .filter(plugin -> plugin.getArtifactId().equals(applicablePlugin.getArtifactId()))
            .collect(Collectors.toList());
        if (!pluginList.isEmpty()) {
            return pluginList.get(0);
        }
        return null;
    }

    private static boolean containsPattern(String testClasses) {
        return Arrays.stream(testClasses.split(","))
            .map(TEST_CLASS_PATTERN::matcher)
            .anyMatch(Matcher::find);
    }
}
