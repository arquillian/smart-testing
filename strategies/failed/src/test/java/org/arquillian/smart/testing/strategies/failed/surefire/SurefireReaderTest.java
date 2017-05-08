package org.arquillian.smart.testing.strategies.failed.surefire;

import java.util.Set;
import javax.xml.stream.XMLStreamException;
import org.arquillian.smart.testing.strategies.failed.TestResult;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class SurefireReaderTest {

    @Test
    public void should_read_test_class_with_failures() throws XMLStreamException {
        final Set<TestResult> testResults =
            SurefireReader.loadTestResults(SurefireReader.class.getResourceAsStream("/surefire-with-failure.xml"));

        assertThat(testResults)
            .extracting(TestResult::getClassName, TestResult::getResult)
            .containsExactlyInAnyOrder(tuple("org.arquillian.smart.testing.strategies.affected.ClassFileIndexTest", TestResult.Result.FAILURE),
                tuple("org.arquillian.smart.testing.strategies.affected.ClassFileIndexTest", TestResult.Result.PASSED));

    }

}
