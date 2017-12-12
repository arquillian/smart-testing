package org.arquillian.smart.testing.mvn.ext.dependencies;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.arquillian.smart.testing.mvn.ext.ApplicablePlugins;

class ModelUtil {

    static Model prepareModelWithSurefirePlugin(String version) {
        Model model = new Model();
        model.setBuild(prepareBuildWithSurefirePlugin(version));

        return model;
    }

    static Build prepareBuildWithSurefirePlugin(String version) {
        Plugin surefirePlugin = new Plugin();
        surefirePlugin.setArtifactId(ApplicablePlugins.SUREFIRE.getArtifactId());
        surefirePlugin.setVersion(version);

        Build build = new Build();
        build.addPlugin(surefirePlugin);

        return build;
    }
}
