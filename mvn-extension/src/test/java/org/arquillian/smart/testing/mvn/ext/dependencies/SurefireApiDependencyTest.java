package org.arquillian.smart.testing.mvn.ext.dependencies;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.mvn.ext.ApplicablePlugins;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SurefireApiDependencyTest {

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

    private Model prepareModelWithSurefirePlugin(String version) {
        System.setProperty("smart.testing", "new");
        System.setProperty("smart.testing.strategy.new",
            "org.arquillian.smart.testing:strategy-changed:0.0.5-SNAPSHOT");

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
