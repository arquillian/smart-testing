package org.arquillian.smart.testing.mvn.ext.checker;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.mvn.ext.ApplicablePlugins;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.arquillian.smart.testing.mvn.ext.ApplicablePlugins.FAILSAFE;
import static org.arquillian.smart.testing.mvn.ext.ApplicablePlugins.SUREFIRE;

public class SkipModuleChecker {

    private final Model model;
    private final Plugin surefirePlugin;
    private final Plugin failsafePlugin;

    public static final String SKIP_TESTS = "skipTests";
    public static final String SKIP_ITs = "skipITs";
    public static final String SKIP = "skip";
    public static final String MAVEN_TEST_SKIP = "maven.test.skip";

    public SkipModuleChecker(Model model) {
        this.model = model;
        this.surefirePlugin = getPlugin(SUREFIRE);
        this.failsafePlugin = getPlugin(FAILSAFE);
    }

    public boolean areIntegrationTestsSkipped() {
        return Boolean.valueOf(System.getProperty(SKIP_ITs)) || isSkipITsSetInPom();
    }

    public boolean areUnitTestsSkipped() {
        if (surefirePlugin != null) {
            Xpp3Dom surefirePluginConfiguration = (Xpp3Dom) surefirePlugin.getConfiguration();
            if (surefirePluginConfiguration != null) {
                return isPropertySetInPluginConfiguration(surefirePluginConfiguration, SKIP) ||
                    isPropertySetInPluginConfiguration(surefirePluginConfiguration, SKIP_TESTS);
            }
        }
        return false;
    }

    public boolean areAllTestsSkipped() {
        return isPropertyInPom(MAVEN_TEST_SKIP) || isPropertyInPom(SKIP_TESTS);
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
}
