package org.arquillian.smart.testing.spi;

import java.io.InputStream;
import java.util.Set;


public interface TestResultParser {

    Set<TestResult> parse(InputStream surefireInputStream);
    String type();
}
