package org.arquillian.smart.testing.parser.junit;

import java.util.Set;
import org.arquillian.smart.testing.spi.TestResult;
import org.arquillian.smart.testing.spi.TestResultParser;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class JUnitTestResultParserTest {

    @Test
    public void should_read_test_class_with_failures() {

        TestResultParser junitTestResultParser = new JUnitTestResultParser();

        final Set<TestResult> testResults =
            junitTestResultParser.parse(JUnitTestResultParser.class.getResourceAsStream("/surefire-with-failure.xml"));

        assertThat(testResults)
            .extracting(TestResult::getClassName, TestResult::getResult)
            .containsExactlyInAnyOrder(tuple("org.arquillian.smart.testing.strategies.affected.ClassDependenciesGraphTest", TestResult.Result.FAILURE),
                tuple("org.arquillian.smart.testing.strategies.affected.ClassDependenciesGraphTest", TestResult.Result.PASSED));

    }

}
