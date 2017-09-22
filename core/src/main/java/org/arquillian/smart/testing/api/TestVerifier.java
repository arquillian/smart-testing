package org.arquillian.smart.testing.api;

import java.nio.file.Path;
import org.arquillian.smart.testing.ClassNameExtractor;
import org.arquillian.smart.testing.TestSelection;

/**
 * Determines if a resource is an actual test for a given runtime (e.g. Surefire)
 */
public interface TestVerifier {

    /**
     * Checks whether the given path to a resource represents a test class or not
     *
     * @param resource A path to a resource to be checked
     * @return Whether the given path to a resource represents a test class or not
     */
    default boolean isTest(Path resource) {

        if (!isJavaFile(resource)) {
            return false;
        }

        final String className = new ClassNameExtractor().extractFullyQualifiedName(resource);
        return isTest(className);
    }

    /**
     * Checks whether the given path to a resource represents a non-test class or not
     *
     * @param resource A path to a resource to be checked
     * @return Whether the given path to a resource represents a non-test class or not
     */
    default boolean isCore(Path resource) {
        if (!isJavaFile(resource)) {
            return false;
        }
        return !isTest(resource);
    }

    default boolean isJavaFile(Path file) {
        return file.toString().endsWith(".java");
    }

    /**
     * Check whether the given {@link TestSelection} instance represents a test class or not
     *
     * @param testSelection A {@link TestSelection} instance to be checked
     * @return Whether the given {@link TestSelection} instance represents a test class or not
     */
    default boolean isTest(TestSelection testSelection) {
        return isTest(testSelection.getClassName());
    }

    /**
     * Checks whether the given name represents a test class or not
     *
     * @param className A name of a class to be checked
     * @return Whether the given name represents a test class or not
     */
    boolean isTest(String className);
}
