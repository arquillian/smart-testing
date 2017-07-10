package org.arquillian.smart.testing.mvn.ext;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = AbstractMavenLifecycleParticipant.class,
    description = "TODO fill it in later", // TODO
    hint = "smart-testing-optimizer")
public class PreBuildTestOptimizer extends AbstractMavenLifecycleParticipant {

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        System.out.println(">>>> Hello smart testers!");
    }
}
