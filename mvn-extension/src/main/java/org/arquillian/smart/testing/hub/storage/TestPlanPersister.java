package org.arquillian.smart.testing.hub.storage;

import java.util.Set;

public interface TestPlanPersister {

    void storeTestPlan(Set<String> testPlan);
}
