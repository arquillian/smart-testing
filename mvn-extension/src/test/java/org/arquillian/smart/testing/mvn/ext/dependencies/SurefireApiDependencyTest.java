package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.Optional;
import net.jcip.annotations.NotThreadSafe;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.mvn.ext.ApplicablePlugins;
import org.assertj.core.groups.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Category(NotThreadSafe.class)
public class SurefireApiDependencyTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_add_surefire_dependency()
        throws Exception {
        // given
        Model model = prepareModelWithSurefirePlugin("2.20");
        final DependencyResolver dependencyResolver = new DependencyResolver(ConfigurationLoader.load());

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
        final Configuration conf = ConfigurationLoader.load();
        final DependencyResolver dependencyResolver = new DependencyResolver(conf);
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

    @Test
    public void should_autocorrect_strategy()
        throws Exception {
        // given
        Model model = prepareModelWithSurefirePlugin("2.20");
        final Configuration conf = configureWithAutocorrect("nwe");
        final DependencyResolver dependencyResolver = new DependencyResolver(conf);
        model.addDependency(new DependencyResolver.SurefireApiDependency("2.20"));

        // when
        dependencyResolver.addRequiredDependencies(model);

        // then
        assertThat(model.getDependencies()).hasSize(3);
        Dependency newDependency = new Dependency();
        newDependency.setGroupId("org.arquillian.smart.testing");
        newDependency.setArtifactId("strategy-new");
        assertThat(model.getDependencies()).extracting("groupId", "artifactId")
            .contains(Tuple.tuple("org.arquillian.smart.testing", "strategy-new"));
        assertThat(conf.getStrategies()).contains("new");

    }

    @Test
    public void should_fail_when_autocorrect_repeats_the_strategy()
        throws Exception {
        // given
        Model model = prepareModelWithSurefirePlugin("2.20");
        final Configuration conf = configureWithAutocorrect("new", "nwe");
        final DependencyResolver dependencyResolver = new DependencyResolver(conf);
        model.addDependency(new DependencyResolver.SurefireApiDependency("2.20"));

        // when
        final Throwable exception = catchThrowable(() -> dependencyResolver.addRequiredDependencies(model));

        // then
        assertThat(exception).isInstanceOf(IllegalStateException.class);

    }

    @Test
    public void should_return_junit5_platform_dependency() {
        // given
        Model model = prepareModelWithSurefirePlugin("2.19.1");
        final Configuration conf = configureWithAutocorrect("new");
        final DependencyResolver dependencyResolver = new DependencyResolver(conf);
        final Plugin plugin = model.getBuild().getPlugins().get(0);
        plugin.addDependency(new DependencyResolver.JUnit5SurefireProviderDependency());

        // when
        final Optional<Dependency> jUnit5PlatformDependency = dependencyResolver.findJUnit5PlatformDependency(plugin);

        // then
        assertThat(jUnit5PlatformDependency).isPresent();

    }

    private Configuration configureWithAutocorrect(String... strategies) {
        final Configuration conf = ConfigurationLoader.load();
        conf.setAutocorrect(true);
        conf.setStrategies(strategies);
        return conf;
    }

    private Model prepareModelWithSurefirePlugin(String version) {
        System.setProperty("smart.testing", "new");
        System.setProperty("smart.testing.strategy.new",
            "org.arquillian.smart.testing:strategy-new:0.0.5-SNAPSHOT");

        Model model = new Model();
        Plugin surefirePlugin = new Plugin();
        surefirePlugin.setArtifactId(ApplicablePlugins.SUREFIRE.getArtifactId());
        surefirePlugin.setVersion(version);

        Build build = new Build();
        build.addPlugin(surefirePlugin);
        model.setBuild(build);

        return model;
    }
}
