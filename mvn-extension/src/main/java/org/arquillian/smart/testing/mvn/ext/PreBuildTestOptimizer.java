package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.hub.TestHub;
import org.arquillian.smart.testing.hub.storage.LocalTestPlanPersister;
import org.arquillian.smart.testing.hub.storage.TestPlanPersister;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = AbstractMavenLifecycleParticipant.class,
    description = "TODO fill it in later", // TODO
    hint = "smart-testing-optimizer")
public class PreBuildTestOptimizer extends AbstractMavenLifecycleParticipant {

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {

        final Configuration configuration = Configuration.read();
        configureExtension(session, configuration);
        final List<TestPlanPersister> testPlanPersisters = Arrays.asList(new LocalTestPlanPersister()); // TODO later SPI ?
        final File projectDir = session.getCurrentProject().getBasedir();
        final String[] strategies = configuration.getStrategies();
        new TestHub(testPlanPersisters).optimize(projectDir, strategies);
    }

    private void configureExtension(MavenSession session, Configuration configuration) {
        // TODO do we want to inject MavenProjectConfigurator instead?

        final MavenProjectConfigurator mavenProjectConfigurator =
            new MavenProjectConfigurator(configuration);
        session.getAllProjects().forEach(mavenProject -> {
            final Model model = mavenProject.getModel();
            mavenProjectConfigurator.addRequiredDependencies(model);
            mavenProjectConfigurator.configureTestRunner(model);
        });
    }
}
