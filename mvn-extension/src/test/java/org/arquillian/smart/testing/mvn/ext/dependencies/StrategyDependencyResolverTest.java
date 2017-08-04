package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.Map;
import net.jcip.annotations.NotThreadSafe;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.mvn.ext.ApplicablePlugins;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
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
        assertThat(dependencies.values()).hasSize(4)
            .extracting(
                dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
            .contains("org.arquillian.smart.testing:smart-testing-strategy-changed:" + ExtensionVersion.version().toString(),
                      "org.arquillian.smart.testing:smart-testing-strategy-failed:" + ExtensionVersion.version().toString(),
                      "org.arquillian.smart.testing:smart-testing-strategy-changed:" + ExtensionVersion.version().toString(),
                      "org.arquillian.smart.testing:smart-testing-strategy-affected:" + ExtensionVersion.version().toString());
    }

    @Test
    public void should_overwrite_default_version_for_new_strategy_when_system_property_is_defined()
        throws Exception {
        // given
        System.setProperty("smart.testing.strategy.new",
            "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.5-SNAPSHOT");

        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver();

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies)
            .hasEntrySatisfying("new",
            dependency -> "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.5-SNAPSHOT".equals(
                dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
        );
    }

    @Test
    public void should_overwrite_default_versions_when_property_file_is_used() throws Exception {
        // given
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(
            get("src/test/resources", "strategies-test.properties"));

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies.values()).hasSize(4)
            .extracting(
                dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
            .contains(
                "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.1-SNAPSHOT", // from default, for new
                "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.11-SNAPSHOT",
                "org.arquillian.smart.testing:smart-testing-strategy-affected:0.0.12-SNAPSHOT",
                "org.arquillian.smart.testing:smart-testing-strategy-failed:0.0.13-SNAPSHOT");
    }

    @Test
    public void should_overwrite_default_properties_by_those_specified_in_the_file_and_then_those_in_system_properties()
        throws Exception {
        // given
        final StrategyDependencyResolver strategyDependencyResolver = new StrategyDependencyResolver(
            get("src/test/resources", "strategies-test.properties"));

        System.setProperty("smart.testing.strategy.affected",
            "org.arquillian.smart.testing:smart-testing-strategy-affected:0.0.2-SNAPSHOT");

        // when
        Map<String, Dependency> dependencies = strategyDependencyResolver.resolveDependencies();

        // then
        assertThat(dependencies.values()).hasSize(4)
            .extracting(
                dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
            .contains(
                "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.1-SNAPSHOT", // from default, for new
                "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.11-SNAPSHOT",
                "org.arquillian.smart.testing:smart-testing-strategy-affected:0.0.2-SNAPSHOT",
                "org.arquillian.smart.testing:smart-testing-strategy-failed:0.0.13-SNAPSHOT");
    }

    @Test
    public void should_add_surefire_dependency()
        throws Exception {
        // given
        Model model = prepareModelWithSurefirePlugin("2.20");
        final DependencyResolver dependencyResolver = new DependencyResolver(Configuration.load());

        // when
        dependencyResolver.addRequiredDependencies(model);

        // then
        assertThat(model.getDependencies()).hasSize(3);
        assertThat(model.getDependencies()).anySatisfy(DependencyResolver.SurefireApiDependency::matches);
        String actualVersion = model.getDependencies()
            .stream()
            .filter(DependencyResolver.SurefireApiDependency::matches)
            .findFirst()
            .get()
            .getVersion();
        assertThat(actualVersion).isEqualTo("2.20");
    }

    @Test
    public void should_not_add_surefire_dependency_as_it_is_present()
        throws Exception {
        // given
        Model model = prepareModelWithSurefirePlugin("2.20");
        final DependencyResolver dependencyResolver = new DependencyResolver(Configuration.load());
        model.addDependency(new DependencyResolver.SurefireApiDependency("2.19.1"));

        // when
        dependencyResolver.addRequiredDependencies(model);

        // then
        assertThat(model.getDependencies()).hasSize(3);
        assertThat(model.getDependencies()).anySatisfy(DependencyResolver.SurefireApiDependency::matches);
        String actualVersion = model.getDependencies()
            .stream()
            .filter(DependencyResolver.SurefireApiDependency::matches)
            .findFirst()
            .get()
            .getVersion();
        assertThat(actualVersion).isEqualTo("2.19.1");
    }

    private Model prepareModelWithSurefirePlugin(String version){
        System.setProperty("smart.testing", "new");
        System.setProperty("smart.testing.strategy.new",
            "org.arquillian.smart.testing:smart-testing-strategy-changed:0.0.5-SNAPSHOT");

        Model model = new Model();
        Plugin surefirePlugin = new Plugin();
        surefirePlugin.setArtifactId(ApplicablePlugins.SUREFIRE.getArtifactId());
        surefirePlugin.setVersion("2.20");
        Build build = new Build();
        build.addPlugin(surefirePlugin);
        model.setBuild(build);

        return model;
    }
}
