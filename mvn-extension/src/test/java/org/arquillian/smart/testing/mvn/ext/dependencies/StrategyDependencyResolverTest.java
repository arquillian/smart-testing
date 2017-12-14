package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.Map;
import net.jcip.annotations.NotThreadSafe;
import org.apache.maven.model.Dependency;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;

import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class StrategyDependencyResolverTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_resolve_dependencies_based_on_default_property_file_when_nothing_more_is_specified() throws Exception {
        // given
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver();

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies.values()).hasSize(5)
            .extracting(
                dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
            .contains("org.arquillian.smart.testing:strategy-changed:" + ExtensionVersion.version().toString(),
                      "org.arquillian.smart.testing:strategy-failed:" + ExtensionVersion.version().toString(),
                      "org.arquillian.smart.testing:strategy-changed:" + ExtensionVersion.version().toString(),
                      "org.arquillian.smart.testing:strategy-affected:" + ExtensionVersion.version().toString(),
                      "org.arquillian.smart.testing:strategy-categorized:" + ExtensionVersion.version().toString());
    }

    @Test
    public void should_overwrite_default_version_for_new_strategy_when_system_property_is_defined()
        throws Exception {
        // given
        System.setProperty("smart.testing.strategy.new",
            "org.arquillian.smart.testing:strategy-changed:0.0.5-SNAPSHOT");

        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver();

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies)
            .hasEntrySatisfying("new",
            dependency -> "org.arquillian.smart.testing:strategy-changed:0.0.5-SNAPSHOT".equals(
                dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
        );
    }

    @Test
    public void should_register_custom_strategies_if_specified_as_array() {
        // given
        String[] customStrategies = new String[] { "smart.testing.strategy.cool=org.arquillian.smart.testing:strategy-cool:1.0.0" };
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(customStrategies);

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies.values()).hasSize(6)
            .extracting(
                dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
            .contains("org.arquillian.smart.testing:strategy-changed:" + ExtensionVersion.version().toString(),
                "org.arquillian.smart.testing:strategy-failed:" + ExtensionVersion.version().toString(),
                "org.arquillian.smart.testing:strategy-affected:" + ExtensionVersion.version().toString(),
                "org.arquillian.smart.testing:strategy-categorized:" + ExtensionVersion.version().toString(),
                "org.arquillian.smart.testing:strategy-cool:1.0.0");
    }

    @Test
    public void should_register_last_custom_strategy_with_same_name() {
        // given
        String[] customStrategies =
            new String[] {"smart.testing.strategy.cool=org.arquillian.smart.testing:strategy-cool:1.0.0",
                "smart.testing.strategy.cool=org.arquillian.smart.testing:strategy-cool:1.0.1"};
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(customStrategies);

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies.values()).hasSize(6)
            .extracting(
                dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
            .contains("org.arquillian.smart.testing:strategy-changed:" + ExtensionVersion.version().toString(),
                "org.arquillian.smart.testing:strategy-failed:" + ExtensionVersion.version().toString(),
                "org.arquillian.smart.testing:strategy-affected:" + ExtensionVersion.version().toString(),
                "org.arquillian.smart.testing:strategy-categorized:" + ExtensionVersion.version().toString(),
                "org.arquillian.smart.testing:strategy-cool:1.0.1");
    }

    @Test
    public void should_register_custom_strategies_if_specified_as_array_with_dots_as_name() {
        // given
        String[] customStrategies = new String[] { "smart.testing.strategy.my.cool=org.arquillian.smart.testing:strategy-cool:1.0.0" };
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(customStrategies);

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies.keySet())
            .containsExactlyInAnyOrder("affected", "changed", "my.cool", "new", "failed", "categorized");
    }
}
