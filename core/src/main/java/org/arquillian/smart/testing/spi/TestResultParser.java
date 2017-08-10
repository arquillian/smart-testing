package org.arquillian.smart.testing.spi;

import java.io.InputStream;
import java.util.Set;

/**
 * Java SPI for parsing test results stored in a stream.
 */
public interface TestResultParser {

    /**
     * Parse given stream and return results in form of smart test model {@link TestResult}
     *
     * This method does not close the stream.
     * @param reportInputStream where results are stored.
     * @return Set of all results parsed.
     */
    Set<TestResult> parse(InputStream reportInputStream);

    /**
     * Type of parser.
     * @return Type of parser such as junit.
     */
    String type();
}
