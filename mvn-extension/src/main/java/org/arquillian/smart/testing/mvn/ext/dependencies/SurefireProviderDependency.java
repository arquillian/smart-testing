package org.arquillian.smart.testing.mvn.ext.dependencies;

import org.apache.maven.model.Dependency;

class SurefireProviderDependency {

    private final Dependency dependency;
    private final SurefireProviderResolver.SurefireProviderDefinition provider;

    SurefireProviderDependency(Dependency dependency, SurefireProviderResolver.SurefireProviderDefinition provider) {
        this.dependency = dependency;
        this.provider = provider;
    }

    String getGAV(){
        return String.join(":", dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    String getProviderClassName() {
        return provider.getProviderClassName();
    }

    Dependency getDependency() {
        return dependency;
    }
}
