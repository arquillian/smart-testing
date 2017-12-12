package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.io.File;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.arquillian.smart.testing.mvn.ext.ApplicablePlugins;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import static org.arquillian.smart.testing.known.surefire.providers.KnownProvider.JUNIT_5;
import static org.arquillian.smart.testing.mvn.ext.dependencies.ModelUtil.prepareBuildWithSurefirePlugin;

public class CustomProvidersTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private File tmpFolder;

    @Before
    public void assignTmpFolder() {
        tmpFolder = temporaryFolder.getRoot();
    }

    @Test
    public void should_remove_junit5_dependency_and_create_info_file() {
        // given
        DependencyResolver dependencyResolver = new DependencyResolver(ConfigurationLoader.load(tmpFolder));
        Dependency junit5Dep = createDependency(JUNIT_5.getGroupId(), JUNIT_5.getArtifactId(), "1.0.1");
        Model model = Mockito.mock(Model.class);
        Plugin plugin = prepareModelWithPluginDep(model, junit5Dep);

        // when
        dependencyResolver.removeAndRegisterFirstCustomProvider(model, plugin);

        // then
        verifyDependencyIsRemovedAndFileCreated(plugin, junit5Dep, JUNIT_5.getProviderClassName());
    }

    @Test
    public void should_remove_custom_provider_dependency_and_create_info_file_when_set_in_config() {
        // given
        Configuration config = ConfigurationLoader.load(tmpFolder);
        config.setCustomProviders(new String[] {"org.foo.provider:my-custom-provider=org.foo.impl.SurefireProvider"});
        DependencyResolver dependencyResolver = new DependencyResolver(config);
        Dependency customDep = createDependency("org.foo.provider", "my-custom-provider", "1.2.3");
        Model model = Mockito.mock(Model.class);
        Plugin plugin = prepareModelWithPluginDep(model, customDep);

        // when
        dependencyResolver.removeAndRegisterFirstCustomProvider(model, plugin);

        // then
        verifyDependencyIsRemovedAndFileCreated(plugin, customDep, "org.foo.impl.SurefireProvider");
    }

    private void verifyDependencyIsRemovedAndFileCreated(Plugin plugin, Dependency providerDep,
        String providerClassName) {
        File providersDir = TemporaryInternalFiles.createCustomProvidersDirAction(tmpFolder,
            ApplicablePlugins.SUREFIRE.getArtifactId()).getFile();
        String providerGAV =
            String.join(":", providerDep.getGroupId(), providerDep.getArtifactId(), providerDep.getVersion());

        softly.assertThat(plugin.getDependencies()).doesNotContain(providerDep);
        softly.assertThat(providersDir).exists().isDirectory();
        softly.assertThat(providersDir.listFiles()).hasSize(1);
        softly.assertThat(providersDir.listFiles()[0]).hasName(providerGAV).hasContent(providerClassName);
    }

    private Plugin prepareModelWithPluginDep(Model model, Dependency junit5Dep) {
        Build build = prepareBuildWithSurefirePlugin("2.19.1");
        Mockito.when(model.getBuild()).thenReturn(build);

        Mockito.when(model.getProjectDirectory()).thenReturn(tmpFolder);
        Plugin plugin = build.getPlugins().get(0);
        plugin.addDependency(junit5Dep);
        return plugin;
    }

    private Dependency createDependency(String groupId, String artifactId, String version) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        return dependency;
    }
}
