package org.arquillian.smart.testing.mvn.ext;

import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.hub.storage.LocalChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.codehaus.plexus.component.annotations.Component;

import static java.util.stream.StreamSupport.stream;

@Component(role = AbstractMavenLifecycleParticipant.class,
    description = "TODO fill it in later", // TODO
    hint = "smart-testing-optimizer")
class PreBuildTestOptimizer extends AbstractMavenLifecycleParticipant {

    private final ChangeStorage changeStorage = new LocalChangeStorage();

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        final Configuration configuration = Configuration.read();
        configureExtension(session, configuration);
        calculateChanges();
    }

    @Override
    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
        System.out.println("Session ends!");
        changeStorage.purgeAll();
    }

    private void calculateChanges() {
        final Iterable<ChangeResolver> changeResolvers = new JavaSPILoader().all(ChangeResolver.class, ChangeResolver::isApplicable);
        final Collection<Change> changes = stream(changeResolvers.spliterator(), false)
            .map(ChangeResolver::diff)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        changeStorage.store(changes); // FIXME DI? dunno
    }

    private void configureExtension(MavenSession session, Configuration configuration) {
        // TODO do we want to inject MavenProjectConfigurator instead?
        // FIXME NPE when we don't specify properties from CLI
        final MavenProjectConfigurator mavenProjectConfigurator =
            new MavenProjectConfigurator(configuration);
        session.getAllProjects().forEach(mavenProject -> {
            final Model model = mavenProject.getModel();
            mavenProjectConfigurator.addRequiredDependencies(model);
            mavenProjectConfigurator.configureTestRunner(model);
        });
    }
}
