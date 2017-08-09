package org.arquillian.smart.testing.filter;

import java.nio.file.Path;

/**
 * Determines if a resource is an actual test for a given runtime (e.g. Surefire)
 */
public interface TestVerifier {

    boolean isTest(Path resource);

    default boolean isCore(Path resource) {
        return !isTest(resource);
    }
}
