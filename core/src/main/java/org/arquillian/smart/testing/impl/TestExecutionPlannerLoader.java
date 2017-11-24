package org.arquillian.smart.testing.impl;

import java.util.Set;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

interface TestExecutionPlannerLoader {

    TestExecutionPlanner getPlannerForStrategy(String strategy);

    TestVerifier getVerifier();

    Set<String> getAvailableStrategyNames();
}
