package org.arquillian.smart.testing.surefire.provider;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;
import org.arquillian.smart.testing.spi.TestResult;
import org.arquillian.smart.testing.spi.TestResultParser;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class SurefireTestResultParserTest {

    @Test
    public void should_read_test_class_with_failures() {

        final Optional<TestResultParser> surefireTestResultParser =
            StreamSupport.stream(ServiceLoader.load(TestResultParser.class).spliterator(), false)
                .filter(trp -> "surefire".equals(trp.type()))
                .findFirst();

        final Set<TestResult> testResults =
            surefireTestResultParser.get().parse(SurefireTestResultParser.class.getResourceAsStream("/surefire-with-failure.xml"));

        assertThat(testResults)
            .extracting(TestResult::getClassName, TestResult::getResult)
            .containsExactlyInAnyOrder(tuple("org.arquillian.smart.testing.strategies.affected.ClassFileIndexTest", TestResult.Result.FAILURE),
                tuple("org.arquillian.smart.testing.strategies.affected.ClassFileIndexTest", TestResult.Result.PASSED));

    }

}
