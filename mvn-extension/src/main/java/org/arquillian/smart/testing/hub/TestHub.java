package org.arquillian.smart.testing.hub;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.StreamSupport;
import org.arquillian.smart.testing.hub.storage.TestPlanPersister;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.arquillian.smart.testing.spi.TestExecutionPlannerFactory;

public class TestHub {

    private final Collection<TestPlanPersister> testPlanPersisters;

    public TestHub(Collection<TestPlanPersister> testPlanPersisters) {
        this.testPlanPersisters = testPlanPersisters;
    }

    public void optimize(File projectDir, String... strategies) {
        final List<String> listOfStrategies = Arrays.asList(strategies);

        // TODO ultimately move TestExecutionPlannerLoader here instead of using this
        final Iterable<TestExecutionPlannerFactory> availableStrategies = new JavaSPILoader().all(TestExecutionPlannerFactory.class,
            testExecutionPlannerFactory -> listOfStrategies.contains(testExecutionPlannerFactory.alias()));

        final Set<String> testClassNames = new ConcurrentSkipListSet<>();

        StreamSupport.stream(availableStrategies.spliterator(), true).forEach(testExecutionPlannerFactory -> {
            final TestExecutionPlanner testExecutionPlanner =
                testExecutionPlannerFactory.create(projectDir, new String[] {"**/*Test*.*"});
            testClassNames.addAll(testExecutionPlanner.getTests());
        });

        testPlanPersisters.parallelStream().forEach(testPlanPersister -> testPlanPersister.storeTestPlan(testClassNames));
    }

}
