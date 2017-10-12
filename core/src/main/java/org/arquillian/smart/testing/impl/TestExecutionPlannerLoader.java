package org.arquillian.smart.testing.impl;

import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;

interface TestExecutionPlannerLoader {

    TestExecutionPlanner getPlannerForStrategy(String strategy, boolean autocorrect);

    TestVerifier getVerifier();
}
