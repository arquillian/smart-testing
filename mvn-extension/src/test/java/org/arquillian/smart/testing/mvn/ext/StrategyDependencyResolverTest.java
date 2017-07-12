package org.arquillian.smart.testing.mvn.ext;

import java.util.Map;
import net.jcip.annotations.NotThreadSafe;
import org.apache.maven.model.Dependency;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class StrategyDependencyResolverTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    // TODO read our own version from manifest file

    @Test
    public void should_resolve_dependency_for_new_strategy_based_on_properties_following_prefix_convention()
        throws Exception {
        // given
        System.setProperty("smart.testing.strategy.new",
            "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.1-SNAPSHOT");

        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver();

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies).hasSize(1)
            .hasEntrySatisfying("new",
            dependency -> "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.1-SNAPSHOT".equals(
                dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
        );
    }

    @Test
    public void should_resolve_dependencies_based_on_properties_following_prefix_convention() throws Exception {
        // given
        System.setProperty("smart.testing.strategy.new",
            "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.1-SNAPSHOT");

        System.setProperty("smart.testing.strategy.affected",
            "org.arquillian.smart.testing:smart-testing-strategy-affected:0.0.1-SNAPSHOT");

        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver();

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies.values()).hasSize(2)
            .extracting(
                dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
            .contains("org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.1-SNAPSHOT",
                "org.arquillian.smart.testing:smart-testing-strategy-affected:0.0.1-SNAPSHOT");
    }

    @Test
    public void should_resolve_dependencies_from_the_property_file() throws Exception {
        // given
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(
            get("src/test/resources", "strategies-test.properties"));

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies.values()).hasSize(3)
            .extracting(
                dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
            .contains("org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.1-SNAPSHOT",
                "org.arquillian.smart.testing:smart-testing-strategy-affected:0.0.1-SNAPSHOT",
                "org.arquillian.smart.testing:smart-testing-strategy-failed:0.0.1-SNAPSHOT");
    }

    @Test
    public void should_resolve_dependencies_from_file_and_system_properties_where_system_properties_takes_precedence()
        throws Exception {
        // given
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(
            get("src/test/resources", "strategies-test.properties"));

        System.setProperty("smart.testing.strategy.affected",
            "org.arquillian.smart.testing:smart-testing-strategy-affected:0.0.2-SNAPSHOT");

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies.values()).hasSize(3)
            .extracting(
                dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
            .contains("org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.1-SNAPSHOT",
                "org.arquillian.smart.testing:smart-testing-strategy-affected:0.0.2-SNAPSHOT",
                "org.arquillian.smart.testing:smart-testing-strategy-failed:0.0.1-SNAPSHOT");
    }
}
