package org.arquillian.smart.testing.api;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.impl.ConfiguredSmartTestingImpl;

/**
 * A class representing a starting point for the usage of the Smart Testing fluent API
 */
public class SmartTesting {

    /**
     * Creates an instance of {@link ConfiguredSmartTesting} class and sets the given instance of {@link TestVerifier}
     * implementation in it
     *
     * @param testVerifier
     *     An instance of {@link TestVerifier} implementation
     *
     * @return An instance of {@link ConfiguredSmartTesting} class
     */
    public static ConfiguredSmartTesting with(TestVerifier testVerifier) {
        return new ConfiguredSmartTestingImpl(testVerifier);
    }

    /**
     * Creates an instance of {@link ConfiguredSmartTesting} class and sets the given instances of {@link TestVerifier}
     * implementation and {@link Configuration} in it
     *
     * @param testVerifier
     *     An instance of {@link TestVerifier} implementation
     * @param configuration
     *     An instance of {@link Configuration}
     *
     * @return An instance of {@link ConfiguredSmartTesting} class
     */
    public static ConfiguredSmartTesting with(TestVerifier testVerifier, Configuration configuration) {
        return new ConfiguredSmartTestingImpl(testVerifier, configuration);
    }

    /**
     * Takes the given set of {@link TestSelection}s and transforms it to the set of class-names
     *
     * @param testSelections The set of {@link TestSelection}s to be transformed
     * @return Retrieved a set of class-names from the given {@link TestSelection}s
     */
    public static Set<String> getNames(Set<TestSelection> testSelections) {
        return mapSelection(testSelections, TestSelection::getClassName);
    }

    /**
     * Takes the given set of {@link TestSelection}s and transforms it to the set of classes
     *
     * @param testSelections The set of {@link TestSelection}s to be transformed
     * @return Retrieved a set of classes from the given {@link TestSelection}s
     */
    public static Set<Class<?>> getClasses(Set<TestSelection> testSelections) {
        return mapSelection(testSelections, SmartTesting::mapToClassInstance);
    }

    private static <RETURNTYPE> Set<RETURNTYPE> mapSelection(Set<TestSelection> testSelections,
        Function<TestSelection, RETURNTYPE> mapper) {
        return testSelections
            .stream()
            .map(mapper)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Class<?> mapToClassInstance(TestSelection testSelection) {
        try {
            return Class.forName(testSelection.getClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
