package org.arquillian.smart.testing.strategies.affected;

import org.arquillian.smart.testing.api.TestVerifier;

import java.nio.file.Path;

public class EndingWithTestTestVerifier implements TestVerifier {

    @Override
    public boolean isTest(Path resource) {
        return isTest(resource.toString());
    }

    @Override
    public boolean isTest(String className) {
        return className.endsWith("Test.java");
    }
}
